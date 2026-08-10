package com.mygaadi.payment.service;

import com.mygaadi.payment.common.ApiException;
import com.razorpay.*;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class RazorpayGatewayService implements RazorpayGateway {
    private final RazorpayClient razorpayClient;

    @Override
    public GatewayOrder createOrder(Long bookingId, Long buyerId, Long sellerId,
                                    BigDecimal amount, String currency) {
        long amountInPaise = toPaise(amount);
        JSONObject request = new JSONObject();
        request.put("amount", amountInPaise);
        request.put("currency", currency);
        request.put("receipt", "booking_" + bookingId);

        JSONObject notes = new JSONObject();
        notes.put("bookingId", bookingId);
        notes.put("buyerId", buyerId);
        notes.put("sellerId", sellerId);
        request.put("notes", notes);

        try {
            Order order = razorpayClient.orders.create(request);
            return new GatewayOrder(stringValue(order.get("id")), amountInPaise,
                    stringValue(order.get("currency")));
        } catch (RazorpayException ex) {
            throw ApiException.serviceUnavailable("Razorpay order creation failed");
        }
    }

    @Override
    public GatewayPayment fetchPayment(String paymentId) {
        try {
            com.razorpay.Payment payment = razorpayClient.payments.fetch(paymentId);
            return new GatewayPayment(
                    stringValue(payment.get("id")),
                    stringValue(payment.get("order_id")),
                    longValue(payment.get("amount")),
                    stringValue(payment.get("currency")),
                    stringValue(payment.get("status")),
                    stringValue(payment.get("method"))
            );
        } catch (RazorpayException ex) {
            throw ApiException.serviceUnavailable("Razorpay payment lookup failed");
        }
    }

    @Override
    public GatewayRefund refund(String paymentId, BigDecimal amount, String currency, Long bookingId) {
        JSONObject request = new JSONObject();
        request.put("amount", toPaise(amount));
        request.put("speed", "normal");
        request.put("receipt", "refund_booking_" + bookingId);
        JSONObject notes = new JSONObject();
        notes.put("bookingId", bookingId);
        request.put("notes", notes);

        try {
            Refund refund = razorpayClient.payments.refund(paymentId, request);
            return new GatewayRefund(stringValue(refund.get("id")), stringValue(refund.get("status")));
        } catch (RazorpayException ex) {
            throw ApiException.serviceUnavailable("Razorpay refund initiation failed");
        }
    }

    private long toPaise(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw ApiException.badRequest("Payment amount must be greater than zero");
        }
        try {
            return amount.setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact();
        } catch (ArithmeticException ex) {
            throw ApiException.badRequest("Payment amount must have at most two decimal places");
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private long longValue(Object value) {
        if (value instanceof Number number) return number.longValue();
        if (value == null) return 0;
        return Long.parseLong(String.valueOf(value));
    }
}
