package com.mygaadi.dto.booking;

import com.mygaadi.dto.listing.CarResponse;
import com.mygaadi.dto.user.UserResponse;
import com.mygaadi.model.entity.Booking;
import com.mygaadi.model.enums.BookingStatus;
import com.mygaadi.model.enums.EscrowStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class BookingResponse {
    private Long id;
    private CarResponse car;
    private UserResponse buyer;
    private UserResponse seller;
    private BigDecimal amount;
    private BigDecimal platformFee;
    private BigDecimal sellerAmount;
    private BookingStatus bookingStatus;
    private EscrowStatus escrowStatus;
    private boolean buyerConfirmed;
    private boolean sellerConfirmed;
    private Instant expiryAt;
    private String notes;
    private Instant createdAt;

    public static BookingResponse from(Booking b) {
        return BookingResponse.builder()
                .id(b.getId())
                .car(CarResponse.from(b.getCar()))
                .buyer(UserResponse.from(b.getBuyer()))
                .seller(UserResponse.from(b.getSeller()))
                .amount(b.getAmount())
                .platformFee(b.getPlatformFee())
                .sellerAmount(b.getSellerAmount())
                .bookingStatus(b.getBookingStatus())
                .escrowStatus(b.getEscrowStatus())
                .buyerConfirmed(b.isBuyerConfirmed())
                .sellerConfirmed(b.isSellerConfirmed())
                .expiryAt(b.getExpiryAt())
                .notes(b.getNotes())
                .createdAt(b.getCreatedAt())
                .build();
    }
}
