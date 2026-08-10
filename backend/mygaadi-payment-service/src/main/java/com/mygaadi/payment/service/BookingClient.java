package com.mygaadi.payment.service;

import com.mygaadi.payment.dto.BookingSnapshot;

public interface BookingClient {
    BookingSnapshot getBooking(Long bookingId, String authorizationHeader);
}
