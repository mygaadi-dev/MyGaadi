package com.mygaadi.payment.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderResponse {
    private Long bookingId;
    private String razorpayOrderId;
    private String keyId;
    private Long amount;
    private String currency;
    private String name;
    private String description;
    private boolean reused;
}
