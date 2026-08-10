package com.mygaadi.payment.service;

import com.mygaadi.payment.entity.Escrow;
import org.springframework.stereotype.Service;

/**
 * Internal ledger implementation only. It does not transfer money to a seller bank account.
 * Replace with a compliant marketplace payout provider before production use.
 */
@Service
public class InternalLedgerPayoutService implements PayoutService {
    @Override
    public String releaseSellerAmount(Escrow escrow) {
        return "INTERNAL-LEDGER-RELEASE-" + escrow.getBookingId();
    }
}
