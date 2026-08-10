package com.mygaadi.payment.repository;

import com.mygaadi.payment.entity.Escrow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface EscrowRepository extends JpaRepository<Escrow, Long> {
    Optional<Escrow> findByBookingId(Long bookingId);
    Optional<Escrow> findByPaymentId(Long paymentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Escrow e where e.bookingId = :bookingId")
    Optional<Escrow> findByBookingIdForUpdate(@Param("bookingId") Long bookingId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Escrow e where e.payment.id = :paymentId")
    Optional<Escrow> findByPaymentIdForUpdate(@Param("paymentId") Long paymentId);
}
