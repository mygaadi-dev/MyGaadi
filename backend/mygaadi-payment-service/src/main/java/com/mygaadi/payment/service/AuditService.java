package com.mygaadi.payment.service;

import com.mygaadi.payment.entity.Payment;
import com.mygaadi.payment.entity.PaymentEvent;
import com.mygaadi.payment.repository.PaymentEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final PaymentEventRepository eventRepository;

    public PaymentEvent recordInternal(Payment payment, String eventType, String safePayload) {
        return eventRepository.save(PaymentEvent.builder()
                .payment(payment)
                .gatewayEventId("internal:" + UUID.randomUUID())
                .eventType(eventType)
                .eventPayload(safePayload)
                .build());
    }


}
