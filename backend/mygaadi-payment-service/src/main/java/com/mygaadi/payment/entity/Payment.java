package com.mygaadi.payment.entity;

import com.mygaadi.payment.enums.PaymentMethod;
import com.mygaadi.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments", uniqueConstraints = {
        @UniqueConstraint(name = "uk_payment_booking", columnNames = "booking_id"),
        @UniqueConstraint(name = "uk_payment_transaction", columnNames = "transaction_id"),
        @UniqueConstraint(name = "uk_payment_razorpay_order", columnNames = "razorpay_order_id"),
        @UniqueConstraint(name = "uk_payment_razorpay_payment", columnNames = "razorpay_payment_id")
}, indexes = {
        @Index(name = "idx_payment_buyer", columnList = "buyer_id"),
        @Index(name = "idx_payment_seller", columnList = "seller_id"),
        @Index(name = "idx_payment_status", columnList = "payment_status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "transaction_id", nullable = false, length = 100)
    private String transactionId;

    @Column(name = "razorpay_order_id", length = 100)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id", length = 100)
    private String razorpayPaymentId;

    @Column(name = "razorpay_signature", length = 255)
    private String razorpaySignature;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    private PaymentStatus paymentStatus;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "gateway_response", columnDefinition = "LONGTEXT")
    private String gatewayResponse;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (paymentMethod == null) paymentMethod = PaymentMethod.UNKNOWN;
        if (currency == null) currency = "INR";
        if (paymentStatus == null) paymentStatus = PaymentStatus.PENDING;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
