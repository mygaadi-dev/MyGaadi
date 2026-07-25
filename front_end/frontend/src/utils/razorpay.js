import { toast } from 'react-toastify';
import api from '../api/api';

export function loadRazorpayScript() {
  return new Promise((resolve) => {
    if (window.Razorpay) return resolve(true);
    const script = document.createElement('script');
    script.src = 'https://checkout.razorpay.com/v1/checkout.js';
    script.onload = () => resolve(true);
    script.onerror = () => resolve(false);
    document.body.appendChild(script);
  });
}

export async function payBookingWithRazorpay(booking, user, onSuccess) {
  const loaded = await loadRazorpayScript();
  if (!loaded) {
    toast.error('Razorpay checkout failed to load');
    return;
  }

  const { data } = await api.post(`/payments/booking/${booking.id}/create-order`);
  const order = data.data;

  const options = {
    key: order.keyId,
    amount: order.amount,
    currency: order.currency,
    name: order.name,
    description: order.description,
    order_id: order.razorpayOrderId,
    prefill: {
      name: user?.name || 'MyGaadi Buyer',
      email: user?.email || 'buyer@mygaadi.com',
      contact: user?.phone || '9999999999',
    },
    theme: { color: '#1A56A4' },
    handler: async (response) => {
      await api.post(`/payments/booking/${booking.id}/verify`, {
        razorpayOrderId: response.razorpay_order_id,
        razorpayPaymentId: response.razorpay_payment_id,
        razorpaySignature: response.razorpay_signature,
      });
      toast.success('Payment verified. Escrow held.');
      onSuccess?.();
    },
  };

  const razorpay = new window.Razorpay(options);
  razorpay.on('payment.failed', () => toast.error('Payment failed'));
  razorpay.open();
}
