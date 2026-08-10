package com.mygaadi.payment.dto;

import com.mygaadi.payment.entity.Payment;
import com.mygaadi.payment.enums.PaymentMethod;
import com.mygaadi.payment.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long bookingId;
    private Long buyerId;
    private Long sellerId;
    private String transactionId;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private PaymentMethod paymentMethod;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus paymentStatus;
    private String failureReason;
    private Instant paidAt;
    private Instant createdAt;
    private Instant updatedAt;
    private List<PaymentEventResponse> events;

    public static PaymentResponse from(Payment payment, List<PaymentEventResponse> events) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .bookingId(payment.getBookingId())
                .buyerId(payment.getBuyerId())
                .sellerId(payment.getSellerId())
                .transactionId(payment.getTransactionId())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .razorpayPaymentId(payment.getRazorpayPaymentId())
                .paymentMethod(payment.getPaymentMethod())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentStatus(payment.getPaymentStatus())
                .failureReason(payment.getFailureReason())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .events(events)
                .build();
    }
}
