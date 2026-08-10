package com.mygaadi.payment.service;

import com.mygaadi.payment.common.ApiException;
import com.mygaadi.payment.entity.Payment;
import com.mygaadi.payment.enums.PaymentMethod;
import com.mygaadi.payment.enums.PaymentStatus;
import com.mygaadi.payment.repository.EscrowRepository;
import com.mygaadi.payment.repository.PaymentEventRepository;
import com.mygaadi.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WebhookServiceTest {
    private PaymentSignatureService signatureService;
    private PaymentRepository paymentRepository;
    private EscrowRepository escrowRepository;
    private PaymentEventRepository eventRepository;
    private PaymentService paymentService;
    private EscrowService escrowService;
    private WebhookService service;

    @BeforeEach
    void setUp() {
        signatureService = mock(PaymentSignatureService.class);
        paymentRepository = mock(PaymentRepository.class);
        escrowRepository = mock(EscrowRepository.class);
        eventRepository = mock(PaymentEventRepository.class);
        paymentService = mock(PaymentService.class);
        escrowService = mock(EscrowService.class);
        service = new WebhookService(signatureService, paymentRepository, escrowRepository,
                eventRepository, paymentService, escrowService);
    }

    @Test
    void duplicateWebhookIsIgnoredSafely() {
        String payload = capturedPayload();
        Payment payment = payment();
        when(signatureService.verifyWebhook(payload, "valid-signature")).thenReturn(true);
        when(paymentRepository.findByRazorpayOrderId("order_123")).thenReturn(Optional.of(payment));
        when(eventRepository.reserveGatewayEvent(51L, "evt_123", "payment.captured", anyString()))
                .thenReturn(0);

        var response = service.process(payload, "valid-signature", "evt_123");

        assertTrue(response.isDuplicate());
        assertFalse(response.isProcessed());
        verifyNoInteractions(paymentService, escrowService);
    }

    @Test
    void invalidWebhookSignatureIsRejected() {
        String payload = capturedPayload();
        when(signatureService.verifyWebhook(payload, "wrong")).thenReturn(false);

        ApiException error = assertThrows(ApiException.class,
                () -> service.process(payload, "wrong", "evt_123"));

        assertEquals(400, error.getStatus().value());
        verifyNoInteractions(paymentRepository, eventRepository);
    }

    private String capturedPayload() {
        return """
                {
                  "event":"payment.captured",
                  "payload":{"payment":{"entity":{
                    "id":"pay_123",
                    "order_id":"order_123",
                    "amount":1000000,
                    "currency":"INR",
                    "status":"captured",
                    "method":"upi"
                  }}}
                }
                """;
    }

    private Payment payment() {
        return Payment.builder()
                .id(51L)
                .bookingId(7L)
                .buyerId(10L)
                .sellerId(20L)
                .transactionId("TXN-1")
                .razorpayOrderId("order_123")
                .paymentMethod(PaymentMethod.UNKNOWN)
                .amount(new BigDecimal("10000.00"))
                .currency("INR")
                .paymentStatus(PaymentStatus.ORDER_CREATED)
                .build();
    }
}
