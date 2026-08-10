package com.mygaadi.payment.service;

import com.mygaadi.payment.common.ApiException;
import com.mygaadi.payment.entity.Escrow;
import com.mygaadi.payment.entity.Payment;
import com.mygaadi.payment.enums.UserRole;
import com.mygaadi.payment.security.JwtPrincipal;
import org.springframework.stereotype.Service;

@Service
public class OwnershipService {
    public void requireBuyer(JwtPrincipal principal, Long buyerId) {
        if (principal.getRole() != UserRole.BUYER || !principal.getUserId().equals(buyerId)) {
            throw ApiException.forbidden("Buyer does not own this resource");
        }
    }

    public void requireSeller(JwtPrincipal principal, Long sellerId) {
        if (principal.getRole() != UserRole.SELLER || !principal.getUserId().equals(sellerId)) {
            throw ApiException.forbidden("Seller does not own this resource");
        }
    }

    public void requireParticipantOrAdmin(JwtPrincipal principal, Payment payment) {
        boolean allowed = principal.getRole() == UserRole.ADMIN
                || (principal.getRole() == UserRole.BUYER && principal.getUserId().equals(payment.getBuyerId()))
                || (principal.getRole() == UserRole.SELLER && principal.getUserId().equals(payment.getSellerId()));
        if (!allowed) throw ApiException.forbidden("You cannot access this payment");
    }

    public void requireParticipantOrAdmin(JwtPrincipal principal, Escrow escrow) {
        boolean allowed = principal.getRole() == UserRole.ADMIN
                || (principal.getRole() == UserRole.BUYER && principal.getUserId().equals(escrow.getBuyerId()))
                || (principal.getRole() == UserRole.SELLER && principal.getUserId().equals(escrow.getSellerId()));
        if (!allowed) throw ApiException.forbidden("You cannot access this escrow");
    }

    public void requireAdmin(JwtPrincipal principal) {
        if (principal.getRole() != UserRole.ADMIN) throw ApiException.forbidden("Admin role required");
    }
}
