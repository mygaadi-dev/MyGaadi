package com.mygaadi.dto.booking;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingRequest {
    @NotNull private Long carId;
    private String notes;
}
