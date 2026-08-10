package com.mygaadi.payment.dto;

import com.mygaadi.payment.enums.BookingStatus;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingSnapshot {
    private Long bookingId;
    private Long buyerId;
    private Long sellerId;
    private BigDecimal amount;
    private BigDecimal platformFee;
    private BigDecimal sellerAmount;
    private BookingStatus bookingStatus;
}
