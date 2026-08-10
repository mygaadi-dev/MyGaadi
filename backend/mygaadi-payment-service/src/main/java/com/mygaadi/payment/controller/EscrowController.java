package com.mygaadi.payment.controller;

import com.mygaadi.payment.common.ApiResponse;
import com.mygaadi.payment.dto.EscrowResponse;
import com.mygaadi.payment.security.CurrentUser;
import com.mygaadi.payment.service.EscrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/escrows")
@RequiredArgsConstructor
@Tag(name = "Escrow")
public class EscrowController {
    private final EscrowService escrowService;
    private final CurrentUser currentUser;

    @PostMapping("/booking/{bookingId}/buyer-confirm")
    @PreAuthorize("hasRole('BUYER')")
    @Operation(summary = "Record buyer confirmation")
    public ApiResponse<EscrowResponse> buyerConfirm(@PathVariable Long bookingId) {
        return ApiResponse.ok("Buyer confirmation recorded",
                escrowService.buyerConfirm(bookingId, currentUser.get()));
    }

    @PostMapping("/booking/{bookingId}/seller-confirm")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Record seller confirmation")
    public ApiResponse<EscrowResponse> sellerConfirm(@PathVariable Long bookingId) {
        return ApiResponse.ok("Seller confirmation recorded",
                escrowService.sellerConfirm(bookingId, currentUser.get()));
    }

    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasAnyRole('BUYER','SELLER','ADMIN')")
    @Operation(summary = "Get escrow status for an owned booking")
    public ApiResponse<EscrowResponse> getStatus(@PathVariable Long bookingId) {
        return ApiResponse.ok("Escrow status",
                escrowService.getByBooking(bookingId, currentUser.get()));
    }
}
