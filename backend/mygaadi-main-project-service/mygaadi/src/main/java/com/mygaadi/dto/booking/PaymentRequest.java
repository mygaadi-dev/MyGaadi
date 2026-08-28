package com.mygaadi.dto.booking;

import com.mygaadi.model.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {
    @NotNull private PaymentMethod paymentMethod;
    private String transactionId;
}
