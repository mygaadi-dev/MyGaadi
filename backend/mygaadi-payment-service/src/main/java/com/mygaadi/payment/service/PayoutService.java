package com.mygaadi.payment.service;

import com.mygaadi.payment.entity.Escrow;

public interface PayoutService {
    String releaseSellerAmount(Escrow escrow);
}
