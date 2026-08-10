package com.mygaadi.payment.controller;

import com.mygaadi.payment.common.ApiResponse;
import com.mygaadi.payment.dto.*;
import com.mygaadi.payment.security.CurrentUser;
import com.mygaadi.payment.service.PaymentService;
import com.mygaadi.payment.service.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final WebhookService webhookService;
    private final CurrentUser currentUser;

    @PostMapping("/booking/{bookingId}/create-order")
    @PreAuthorize("hasRole('BUYER')")
    @Operation(summary = "Create or reuse a Razorpay order for an accepted booking")
    public ApiResponse<CreateOrderResponse> createOrder(
            @PathVariable Long bookingId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestHeader("Authorization") String authorizationHeader) {
        return ApiResponse.ok("Razorpay order ready",
                paymentService.createOrder(bookingId, idempotencyKey,
                        authorizationHeader, currentUser.get()));
    }

    @PostMapping("/booking/{bookingId}/verify")
    @PreAuthorize("hasRole('BUYER')")
    @Operation(summary = "Verify Razorpay signature and hold the internal escrow ledger")
    public ApiResponse<PaymentResponse> verify(
            @PathVariable Long bookingId,
            @Valid @RequestBody VerifyPaymentRequest request) {
        return ApiResponse.ok("Payment verified",
                paymentService.verifyPayment(bookingId, request, currentUser.get()));
    }

    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasAnyRole('BUYER','SELLER','ADMIN')")
    @Operation(summary = "Get payment status for an owned booking")
    public ApiResponse<PaymentResponse> getStatus(@PathVariable Long bookingId) {
        return ApiResponse.ok("Payment status",
                paymentService.getByBooking(bookingId, currentUser.get()));
    }

    @PostMapping("/webhook/razorpay")
    @Operation(summary = "Process a signed Razorpay webhook")
    public ApiResponse<WebhookResponse> webhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature,
            @RequestHeader(value = "X-Razorpay-Event-Id", required = false) String eventId) {
        return ApiResponse.ok("Webhook accepted",
                webhookService.process(payload, signature, eventId));
    }
}
