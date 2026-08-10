package com.mygaadi.payment.service;

import java.math.BigDecimal;

public interface RazorpayGateway {
    GatewayOrder createOrder(Long bookingId, Long buyerId, Long sellerId, BigDecimal amount, String currency);
    GatewayPayment fetchPayment(String paymentId);
    GatewayRefund refund(String paymentId, BigDecimal amount, String currency, Long bookingId);

    record GatewayOrder(String orderId, long amountInPaise, String currency) { }
    record GatewayPayment(String paymentId, String orderId, long amountInPaise,
                          String currency, String status, String method) { }
    record GatewayRefund(String refundId, String status) { }
}
