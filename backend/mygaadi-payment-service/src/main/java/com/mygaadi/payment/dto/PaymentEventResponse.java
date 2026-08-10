package com.mygaadi.payment.dto;

import com.mygaadi.payment.entity.PaymentEvent;
import lombok.*;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEventResponse {
    private String eventType;
    private Instant createdAt;

    public static PaymentEventResponse from(PaymentEvent event) {
        return PaymentEventResponse.builder()
                .eventType(event.getEventType())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
