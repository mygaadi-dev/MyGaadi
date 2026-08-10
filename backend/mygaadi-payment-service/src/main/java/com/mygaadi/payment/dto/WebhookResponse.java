package com.mygaadi.payment.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookResponse {
    private String eventId;
    private String eventType;
    private boolean duplicate;
    private boolean processed;
}
