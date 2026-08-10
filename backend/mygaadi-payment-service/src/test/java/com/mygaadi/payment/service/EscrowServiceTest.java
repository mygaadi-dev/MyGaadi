package com.mygaadi.payment.service;

import com.mygaadi.payment.entity.Escrow;
import com.mygaadi.payment.entity.Payment;
import com.mygaadi.payment.enums.EscrowStatus;
import com.mygaadi.payment.enums.PaymentMethod;
import com.mygaadi.payment.enums.PaymentStatus;
import com.mygaadi.payment.enums.UserRole;
import com.mygaadi.payment.repository.EscrowRepository;
import com.mygaadi.payment.security.JwtPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EscrowServiceTest {
    private EscrowRepository escrowRepository;
    private PayoutService payoutService;
    private AuditService auditService;
    private EscrowService service;

    @BeforeEach
    void setUp() {
        escrowRepository = mock(EscrowRepository.class);
        payoutService = mock(PayoutService.class);
        auditService = mock(AuditService.class);
        service = new EscrowService(escrowRepository, new OwnershipService(), payoutService, auditService);
    }

    @Test
    void holdsEscrowOnlyAfterSuccessfulPaymentDataIsProvided() {
        Payment payment = payment();
        when(escrowRepository.findByBookingIdForUpdate(7L)).thenReturn(Optional.empty());
        when(escrowRepository.save(any(Escrow.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.holdEscrow(payment, 7L,
                new BigDecimal("200.00"), new BigDecimal("9800.00"));

        assertEquals(EscrowStatus.HELD, response.getEscrowStatus());
        assertEquals(new BigDecimal("200.00"), response.getPlatformFee());
        assertEquals(new BigDecimal("9800.00"), response.getSellerAmount());
    }

    @Test
    void releasesOnlyAfterBuyerAndSellerConfirm() {
        Escrow escrow = heldEscrow();
        when(escrowRepository.findByBookingIdForUpdate(7L)).thenReturn(Optional.of(escrow));
        when(payoutService.releaseSellerAmount(escrow)).thenReturn("ledger-reference");

        service.buyerConfirm(7L, principal(10L, UserRole.BUYER));
        assertEquals(EscrowStatus.HELD, escrow.getEscrowStatus());
        verifyNoInteractions(payoutService);

        service.sellerConfirm(7L, principal(20L, UserRole.SELLER));
        assertEquals(EscrowStatus.RELEASED, escrow.getEscrowStatus());
        assertNotNull(escrow.getReleasedAt());
        verify(payoutService, times(1)).releaseSellerAmount(escrow);

        service.sellerConfirm(7L, principal(20L, UserRole.SELLER));
        verify(payoutService, times(1)).releaseSellerAmount(escrow);
    }

    private Escrow heldEscrow() {
        return Escrow.builder()
                .id(31L)
                .payment(payment())
                .bookingId(7L)
                .buyerId(10L)
                .sellerId(20L)
                .totalAmount(new BigDecimal("10000.00"))
                .platformFee(new BigDecimal("200.00"))
                .sellerAmount(new BigDecimal("9800.00"))
                .escrowStatus(EscrowStatus.HELD)
                .version(0L)
                .build();
    }

    private Payment payment() {
        return Payment.builder()
                .id(51L)
                .bookingId(7L)
                .buyerId(10L)
                .sellerId(20L)
                .transactionId("TXN-1")
                .razorpayOrderId("order_123")
                .razorpayPaymentId("pay_123")
                .paymentMethod(PaymentMethod.UPI)
                .amount(new BigDecimal("10000.00"))
                .currency("INR")
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();
    }

    private JwtPrincipal principal(Long id, UserRole role) {
        return JwtPrincipal.builder()
                .userId(id)
                .email(role.name().toLowerCase() + "@test.com")
                .role(role)
                .build();
    }
}
