import { useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'react-toastify';
import api, { unwrap } from '../../api/api';
import DashboardLayout from '../../components/DashboardLayout';
import EmptyState from '../../components/EmptyState';
import { Car, CreditCard, CheckCircle, Clock, ShieldCheck } from '../../components/AppIcons.jsx';
import { useAuth } from '../../state/AuthContext';
import { payBookingWithRazorpay } from '../../utils/razorpay';

export default function BuyerBookings(){
  const qc=useQueryClient();
  const { user } = useAuth();
  const {data=[]}=useQuery({queryKey:['buyerBookings'],queryFn:()=>api.get('/bookings/buyer/me').then(unwrap)});
  const act=async(id,path)=>{await api.post(`/bookings/${id}/${path}`);toast.success('Updated');qc.invalidateQueries({queryKey:['buyerBookings']});};
  const pay = (booking) => payBookingWithRazorpay(booking, user, () => qc.invalidateQueries({queryKey:['buyerBookings']}));

  const statusIcon = (status) => {
    if (status === 'COMPLETED') return <CheckCircle className="w-4 h-4 text-accent" />;
    if (status === 'ACCEPTED') return <CreditCard className="w-4 h-4 text-primary" />;
    if (status === 'CONFIRMED') return <ShieldCheck className="w-4 h-4 text-accent" />;
    return <Clock className="w-4 h-4 text-warning" />;
  };

  return <DashboardLayout title="My Bookings">
    {data.length? (
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
        {data.map(b=>
          <div className="dash-card" key={b.id}>
            <div className="flex items-start justify-between mb-3">
              <div className="flex items-center gap-2">
                <div className="w-8 h-8 rounded-lg bg-blue-50 flex items-center justify-center"><Car className="w-4 h-4 text-primary" /></div>
                <h5 className="font-bold text-slate-800">{b.car.title}</h5>
              </div>
              <b className="text-accent">₹{Number(b.amount).toLocaleString('en-IN')}</b>
            </div>
            <div className="flex flex-wrap items-center gap-2 text-sm text-text-muted mb-4">
              <span className="px-2 py-1 rounded-lg bg-blue-50 text-primary text-xs font-bold">Booking: {b.bookingStatus}</span>
              <span className="px-2 py-1 rounded-lg bg-emerald-50 text-emerald-700 text-xs font-bold">Escrow: {b.escrowStatus}</span>
            </div>
            <div className="flex items-center gap-2 mb-4">
              <span className="timeline-dot"></span>
              <span className="text-sm font-medium text-slate-700 flex items-center gap-1">{statusIcon(b.bookingStatus)} {b.bookingStatus}</span>
            </div>
            <div className="flex flex-wrap gap-2">
              {b.bookingStatus==='ACCEPTED'&&<button className="px-4 py-2 rounded-lg btn-accent text-sm font-semibold" onClick={()=>pay(b)}>Pay with Razorpay</button>}
              {b.bookingStatus==='CONFIRMED'&&!b.buyerConfirmed&&<button className="px-4 py-2 rounded-lg btn-gradient text-sm font-semibold" onClick={()=>act(b.id,'buyer-confirm')}>Confirm Delivery</button>}
            </div>
          </div>
        )}
      </div>
    ) : <EmptyState title="No bookings" />}
  </DashboardLayout>;
}
