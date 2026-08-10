package com.mygaadi.payment.dto;

import com.mygaadi.payment.enums.BookingStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExistingBookingResponse {
    private Long id;
    private ExistingUserResponse buyer;
    private ExistingUserResponse seller;
    private BigDecimal amount;
    private BigDecimal platformFee;
    private BigDecimal sellerAmount;
    private BookingStatus bookingStatus;

    @Data
    public static class ExistingUserResponse {
        private Long id;
    }

    public BookingSnapshot toSnapshot() {
        return BookingSnapshot.builder()
                .bookingId(id)
                .buyerId(buyer == null ? null : buyer.getId())
                .sellerId(seller == null ? null : seller.getId())
                .amount(amount)
                .platformFee(platformFee)
                .sellerAmount(sellerAmount)
                .bookingStatus(bookingStatus)
                .build();
    }
}
