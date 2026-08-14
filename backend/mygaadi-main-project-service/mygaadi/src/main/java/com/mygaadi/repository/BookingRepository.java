package com.mygaadi.repository;

import com.mygaadi.model.entity.Booking;
import com.mygaadi.model.entity.Car;
import com.mygaadi.model.entity.User;
import com.mygaadi.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBuyerAndDeletedFalseOrderByCreatedAtDesc(User buyer);
    List<Booking> findBySellerAndDeletedFalseOrderByCreatedAtDesc(User seller);
    boolean existsByCarAndBookingStatusIn(Car car, List<BookingStatus> statuses);
}
