package com.mygaadi.repository;

import com.mygaadi.model.entity.Booking;
import com.mygaadi.model.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBookingAndDeletedFalseOrderByCreatedAtDesc(Booking booking);
    boolean existsByTransactionId(String transactionId);
    Optional<Payment> findFirstByBookingAndDeletedFalseOrderByCreatedAtDesc(Booking booking);
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);
}
