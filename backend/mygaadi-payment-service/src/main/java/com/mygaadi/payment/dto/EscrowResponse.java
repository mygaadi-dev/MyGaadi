package com.mygaadi.payment.dto;

import com.mygaadi.payment.entity.Escrow;
import com.mygaadi.payment.enums.EscrowStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscrowResponse {
    private Long id;
    private Long paymentId;
    private Long bookingId;
    private Long buyerId;
    private Long sellerId;
    private BigDecimal totalAmount;
    private BigDecimal platformFee;
    private BigDecimal sellerAmount;
    private EscrowStatus escrowStatus;
    private boolean buyerConfirmed;
    private boolean sellerConfirmed;
    private Instant heldAt;
    private Instant releasedAt;
    private Instant refundedAt;
    private Instant createdAt;
    private Instant updatedAt;
    private Long version;

    public static EscrowResponse from(Escrow escrow) {
        return EscrowResponse.builder()
                .id(escrow.getId())
                .paymentId(escrow.getPayment().getId())
                .bookingId(escrow.getBookingId())
                .buyerId(escrow.getBuyerId())
                .sellerId(escrow.getSellerId())
                .totalAmount(escrow.getTotalAmount())
                .platformFee(escrow.getPlatformFee())
                .sellerAmount(escrow.getSellerAmount())
                .escrowStatus(escrow.getEscrowStatus())
                .buyerConfirmed(escrow.isBuyerConfirmed())
                .sellerConfirmed(escrow.isSellerConfirmed())
                .heldAt(escrow.getHeldAt())
                .releasedAt(escrow.getReleasedAt())
                .refundedAt(escrow.getRefundedAt())
                .createdAt(escrow.getCreatedAt())
                .updatedAt(escrow.getUpdatedAt())
                .version(escrow.getVersion())
                .build();
    }
}
