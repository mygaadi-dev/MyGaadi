package com.mygaadi.model.entity;

import com.mygaadi.model.enums.BookingStatus;
import com.mygaadi.model.enums.EscrowStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_booking_buyer", columnList = "buyer_id"),
        @Index(name = "idx_booking_seller", columnList = "seller_id"),
        @Index(name = "idx_booking_status", columnList = "booking_status, escrow_status")
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "platform_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal platformFee;

    @Column(name = "seller_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal sellerAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false, length = 20)
    private BookingStatus bookingStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "escrow_status", nullable = false, length = 20)
    private EscrowStatus escrowStatus;

    @Column(name = "buyer_confirmed", nullable = false)
    private boolean buyerConfirmed;

    @Column(name = "seller_confirmed", nullable = false)
    private boolean sellerConfirmed;

    @Column(name = "expiry_at")
    private Instant expiryAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (bookingStatus == null) bookingStatus = BookingStatus.INITIATED;
        if (escrowStatus == null) escrowStatus = EscrowStatus.PENDING;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
