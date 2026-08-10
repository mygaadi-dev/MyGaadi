package com.mygaadi.payment.service;

import com.mygaadi.payment.common.ApiException;
import com.mygaadi.payment.dto.EscrowResponse;
import com.mygaadi.payment.entity.Escrow;
import com.mygaadi.payment.entity.Payment;
import com.mygaadi.payment.enums.EscrowStatus;
import com.mygaadi.payment.enums.PaymentStatus;
import com.mygaadi.payment.repository.EscrowRepository;
import com.mygaadi.payment.security.JwtPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class EscrowService {
    private final EscrowRepository escrowRepository;
    private final OwnershipService ownershipService;
    private final PayoutService payoutService;
    private final AuditService auditService;

    @Transactional
    public EscrowResponse holdEscrow(Payment payment, Long bookingId,
                                     java.math.BigDecimal platformFee,
                                     java.math.BigDecimal sellerAmount) {
        if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw ApiException.badRequest("Escrow can be held only after successful payment verification");
        }
        if (platformFee == null || sellerAmount == null
                || platformFee.signum() < 0 || sellerAmount.signum() < 0
                || platformFee.add(sellerAmount).compareTo(payment.getAmount()) != 0) {
            throw ApiException.badRequest("Escrow amount breakdown is invalid");
        }

        Escrow existing = escrowRepository.findByBookingIdForUpdate(bookingId).orElse(null);
        if (existing != null) {
            if (existing.getEscrowStatus() == EscrowStatus.HELD) {
                return EscrowResponse.from(existing);
            }
            throw ApiException.conflict("Escrow already exists in state " + existing.getEscrowStatus());
        }

        Escrow escrow = Escrow.builder()
                .payment(payment)
                .bookingId(payment.getBookingId())
                .buyerId(payment.getBuyerId())
                .sellerId(payment.getSellerId())
                .totalAmount(payment.getAmount())
                .platformFee(platformFee)
                .sellerAmount(sellerAmount)
                .escrowStatus(EscrowStatus.HELD)
                .heldAt(Instant.now())
                .build();

        escrowRepository.save(escrow);
        auditService.recordInternal(payment, "ESCROW_HELD", "{\"bookingId\":" + bookingId + "}");
        return EscrowResponse.from(escrow);
    }

    @Transactional(readOnly = true)
    public EscrowResponse getByBooking(Long bookingId, JwtPrincipal principal) {
        Escrow escrow = escrowRepository.findByBookingId(bookingId)
                .orElseThrow(() -> ApiException.notFound("Escrow not found"));
        ownershipService.requireParticipantOrAdmin(principal, escrow);
        return EscrowResponse.from(escrow);
    }

    @Transactional
    public EscrowResponse buyerConfirm(Long bookingId, JwtPrincipal buyer) {
        Escrow escrow = lock(bookingId);
        ownershipService.requireBuyer(buyer, escrow.getBuyerId());
        if (escrow.getEscrowStatus() == EscrowStatus.RELEASED) {
            return EscrowResponse.from(escrow);
        }
        requireHeld(escrow);
        if (!escrow.isBuyerConfirmed()) {
            escrow.setBuyerConfirmed(true);
            auditService.recordInternal(escrow.getPayment(), "BUYER_CONFIRMED", "{}");
        }
        releaseWhenBothConfirmed(escrow);
        return EscrowResponse.from(escrow);
    }

    @Transactional
    public EscrowResponse sellerConfirm(Long bookingId, JwtPrincipal seller) {
        Escrow escrow = lock(bookingId);
        ownershipService.requireSeller(seller, escrow.getSellerId());
        if (escrow.getEscrowStatus() == EscrowStatus.RELEASED) {
            return EscrowResponse.from(escrow);
        }
        requireHeld(escrow);
        if (!escrow.isSellerConfirmed()) {
            escrow.setSellerConfirmed(true);
            auditService.recordInternal(escrow.getPayment(), "SELLER_CONFIRMED", "{}");
        }
        releaseWhenBothConfirmed(escrow);
        return EscrowResponse.from(escrow);
    }

    public void completeRefund(Payment payment, Escrow escrow, String source) {
        if (escrow.getEscrowStatus() == EscrowStatus.REFUNDED) return;
        if (escrow.getEscrowStatus() == EscrowStatus.RELEASED) {
            throw ApiException.conflict("Released escrow cannot be refunded");
        }
        payment.setPaymentStatus(PaymentStatus.REFUNDED);
        escrow.setEscrowStatus(EscrowStatus.REFUNDED);
        escrow.setRefundedAt(Instant.now());
        auditService.recordInternal(payment, "REFUND_COMPLETED", "{\"source\":\"" + safe(source) + "\"}");
    }

    private Escrow lock(Long bookingId) {
        return escrowRepository.findByBookingIdForUpdate(bookingId)
                .orElseThrow(() -> ApiException.notFound("Escrow not found"));
    }

    private void requireHeld(Escrow escrow) {
        if (escrow.getEscrowStatus() != EscrowStatus.HELD) {
            throw ApiException.badRequest("Escrow must be HELD for this operation");
        }
    }

    private void releaseWhenBothConfirmed(Escrow escrow) {
        if (escrow.isBuyerConfirmed() && escrow.isSellerConfirmed()) {
            release(escrow, "BOTH_PARTIES_CONFIRMED");
        }
    }

    private void release(Escrow escrow, String source) {
        if (escrow.getEscrowStatus() == EscrowStatus.RELEASED) return;
        if (escrow.getEscrowStatus() == EscrowStatus.REFUNDED
                || escrow.getEscrowStatus() == EscrowStatus.REFUND_PENDING) {
            throw ApiException.conflict("Refunded escrow cannot be released");
        }
        String reference = payoutService.releaseSellerAmount(escrow);
        escrow.setEscrowStatus(EscrowStatus.RELEASED);
        escrow.setReleasedAt(Instant.now());
        auditService.recordInternal(escrow.getPayment(), "ESCROW_RELEASED",
                "{\"source\":\"" + safe(source) + "\",\"reference\":\"" + safe(reference) + "\"}");
    }

    private String safe(String value) {
        return value == null ? "" : value.replace("\\", "").replace("\"", "");
    }
}
