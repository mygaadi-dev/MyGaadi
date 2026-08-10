package com.mygaadi.payment.repository;

import com.mygaadi.payment.entity.PaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentEventRepository extends JpaRepository<PaymentEvent, Long> {
    List<PaymentEvent> findByPaymentIdOrderByCreatedAtAsc(Long paymentId);

    @Modifying
    @Query(value = """
            INSERT IGNORE INTO payment_events
            (payment_id, gateway_event_id, event_type, event_payload, created_at)
            VALUES (:paymentId, :gatewayEventId, :eventType, :eventPayload, CURRENT_TIMESTAMP(6))
            """, nativeQuery = true)
    int reserveGatewayEvent(@Param("paymentId") Long paymentId,
                            @Param("gatewayEventId") String gatewayEventId,
                            @Param("eventType") String eventType,
                            @Param("eventPayload") String eventPayload);
}
