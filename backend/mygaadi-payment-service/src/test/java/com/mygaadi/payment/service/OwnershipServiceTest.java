package com.mygaadi.payment.service;

import com.mygaadi.payment.common.ApiException;
import com.mygaadi.payment.entity.Payment;
import com.mygaadi.payment.enums.UserRole;
import com.mygaadi.payment.security.JwtPrincipal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OwnershipServiceTest {
    private final OwnershipService service = new OwnershipService();
    private final Payment payment = Payment.builder().buyerId(10L).sellerId(20L).build();

    @Test
    void buyerAndSellerCanViewOnlyOwnedPayment() {
        assertDoesNotThrow(() -> service.requireParticipantOrAdmin(principal(10L, UserRole.BUYER), payment));
        assertDoesNotThrow(() -> service.requireParticipantOrAdmin(principal(20L, UserRole.SELLER), payment));
        assertThrows(ApiException.class,
                () -> service.requireParticipantOrAdmin(principal(11L, UserRole.BUYER), payment));
        assertThrows(ApiException.class,
                () -> service.requireParticipantOrAdmin(principal(21L, UserRole.SELLER), payment));
    }

    @Test
    void adminCanViewButBuyerCannotActAsSeller() {
        assertDoesNotThrow(() -> service.requireParticipantOrAdmin(principal(99L, UserRole.ADMIN), payment));
        assertThrows(ApiException.class, () -> service.requireSeller(principal(10L, UserRole.BUYER), 20L));
    }

    private JwtPrincipal principal(Long id, UserRole role) {
        return JwtPrincipal.builder().userId(id).email(role.name().toLowerCase() + "@test.com").role(role).build();
    }
}
