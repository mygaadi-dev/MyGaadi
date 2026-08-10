package com.mygaadi.payment.entity;

import com.mygaadi.payment.enums.EscrowStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "escrows", uniqueConstraints = {
        @UniqueConstraint(name = "uk_escrow_payment", columnNames = "payment_id"),
        @UniqueConstraint(name = "uk_escrow_booking", columnNames = "booking_id")
}, indexes = {
        @Index(name = "idx_escrow_buyer", columnList = "buyer_id"),
        @Index(name = "idx_escrow_seller", columnList = "seller_id"),
        @Index(name = "idx_escrow_status", columnList = "escrow_status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Escrow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "platform_fee", nullable = false, precision = 14, scale = 2)
    private BigDecimal platformFee;

    @Column(name = "seller_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal sellerAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "escrow_status", nullable = false, length = 30)
    private EscrowStatus escrowStatus;

    @Column(name = "buyer_confirmed", nullable = false)
    private boolean buyerConfirmed;

    @Column(name = "seller_confirmed", nullable = false)
    private boolean sellerConfirmed;

    @Column(name = "held_at")
    private Instant heldAt;

    @Column(name = "released_at")
    private Instant releasedAt;

    @Column(name = "refunded_at")
    private Instant refundedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (escrowStatus == null) escrowStatus = EscrowStatus.PENDING;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
