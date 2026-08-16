package com.mygaadi.model.entity;

import com.mygaadi.model.enums.PaymentMethod;
import com.mygaadi.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments", indexes = @Index(name = "idx_payment_status", columnList = "status"))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "transaction_id", nullable = false, unique = true, length = 160)
    private String transactionId;

    @Column(name = "razorpay_order_id", unique = true, length = 100)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id", unique = true, length = 100)
    private String razorpayPaymentId;

    @Column(name = "razorpay_signature", length = 255)
    private String razorpaySignature;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "gateway_name", length = 80)
    private String gatewayName;

    @Column(name = "gateway_response", columnDefinition = "JSON")
    private String gatewayResponse;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        if (currency == null) currency = "INR";
        if (status == null) status = PaymentStatus.PENDING;
    }
}
