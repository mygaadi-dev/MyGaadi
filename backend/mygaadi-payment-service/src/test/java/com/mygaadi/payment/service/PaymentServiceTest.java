package com.mygaadi.payment.service;

import com.mygaadi.payment.common.ApiException;
import com.mygaadi.payment.config.RazorpayConfig;
import com.mygaadi.payment.dto.*;
import com.mygaadi.payment.entity.Payment;
import com.mygaadi.payment.enums.*;
import com.mygaadi.payment.repository.PaymentEventRepository;
import com.mygaadi.payment.repository.PaymentRepository;
import com.mygaadi.payment.security.JwtPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {
    private PaymentRepository paymentRepository;
    private PaymentEventRepository eventRepository;
    private BookingClient bookingClient;
    private RazorpayGateway gateway;
    private PaymentSignatureService signatureService;
    private EscrowService escrowService;
    private AuditService auditService;
    private PaymentService service;

    private final OwnershipService ownershipService = new OwnershipService();
    private final JwtPrincipal buyer = JwtPrincipal.builder()
            .userId(10L).email("buyer@mygaadi.com").role(UserRole.BUYER).build();

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        eventRepository = mock(PaymentEventRepository.class);
        bookingClient = mock(BookingClient.class);
        gateway = mock(RazorpayGateway.class);
        signatureService = mock(PaymentSignatureService.class);
        escrowService = mock(EscrowService.class);
        auditService = mock(AuditService.class);
        RazorpayConfig config = new RazorpayConfig();
        config.setKeyId("rzp_test_key");
        config.setCurrency("INR");
        service = new PaymentService(paymentRepository, eventRepository, bookingClient,
                gateway, config, signatureService, escrowService, auditService, ownershipService);
    }

    @Test
    void rejectsBuyerWhoDoesNotOwnBooking() {
        BookingSnapshot snapshot = acceptedBooking();
        snapshot.setBuyerId(99L);
        when(bookingClient.getBooking(7L, "Bearer token")).thenReturn(snapshot);

        ApiException error = assertThrows(ApiException.class,
                () -> service.createOrder(7L, "key", "Bearer token", buyer));

        assertEquals(403, error.getStatus().value());
        verifyNoInteractions(gateway);
    }

    @Test
    void preventsSecondPaymentForCompletedBooking() {
        when(bookingClient.getBooking(7L, "Bearer token")).thenReturn(acceptedBooking());
        Payment completed = payment(PaymentStatus.SUCCESS);
        when(paymentRepository.findByBookingIdForUpdate(7L)).thenReturn(Optional.of(completed));

        ApiException error = assertThrows(ApiException.class,
                () -> service.createOrder(7L, "same-key", "Bearer token", buyer));

        assertEquals(409, error.getStatus().value());
        verifyNoInteractions(gateway);
    }

    @Test
    void reusesExistingOrderInsteadOfCreatingDuplicate() {
        when(bookingClient.getBooking(7L, "Bearer token")).thenReturn(acceptedBooking());
        Payment existing = payment(PaymentStatus.ORDER_CREATED);
        existing.setRazorpayOrderId("order_existing");
        when(paymentRepository.findByBookingIdForUpdate(7L)).thenReturn(Optional.of(existing));

        CreateOrderResponse response = service.createOrder(7L, "same-key", "Bearer token", buyer);

        assertTrue(response.isReused());
        assertEquals("order_existing", response.getRazorpayOrderId());
        verifyNoInteractions(gateway);
    }

    @Test
    void successfulVerificationCreatesHeldEscrow() {
        Payment payment = payment(PaymentStatus.ORDER_CREATED);
        payment.setId(51L);
        payment.setRazorpayOrderId("order_123");
        payment.setGatewayResponse("{\"orderId\":\"order_123\",\"platformFee\":\"200.00\",\"sellerAmount\":\"9800.00\"}");
        when(paymentRepository.findByBookingIdForUpdate(7L)).thenReturn(Optional.of(payment));
        when(signatureService.verifyPayment("order_123", "pay_123", "signature")).thenReturn(true);
        when(gateway.fetchPayment("pay_123")).thenReturn(new RazorpayGateway.GatewayPayment(
                "pay_123", "order_123", 1_000_000L, "INR", "captured", "upi"));
        when(eventRepository.findByPaymentIdOrderByCreatedAtAsc(51L)).thenReturn(List.of());

        VerifyPaymentRequest request = new VerifyPaymentRequest();
        request.setRazorpayOrderId("order_123");
        request.setRazorpayPaymentId("pay_123");
        request.setRazorpaySignature("signature");

        PaymentResponse response = service.verifyPayment(7L, request, buyer);

        assertEquals(PaymentStatus.SUCCESS, response.getPaymentStatus());
        assertEquals("pay_123", response.getRazorpayPaymentId());
        verify(escrowService).holdEscrow(payment, 7L,
                new BigDecimal("200.00"), new BigDecimal("9800.00"));
    }

    @Test
    void invalidRazorpaySignatureIsRejected() {
        Payment payment = payment(PaymentStatus.ORDER_CREATED);
        payment.setRazorpayOrderId("order_123");
        when(paymentRepository.findByBookingIdForUpdate(7L)).thenReturn(Optional.of(payment));
        when(signatureService.verifyPayment(anyString(), anyString(), anyString())).thenReturn(false);

        VerifyPaymentRequest request = new VerifyPaymentRequest();
        request.setRazorpayOrderId("order_123");
        request.setRazorpayPaymentId("pay_123");
        request.setRazorpaySignature("wrong");

        ApiException error = assertThrows(ApiException.class,
                () -> service.verifyPayment(7L, request, buyer));

        assertEquals(400, error.getStatus().value());
        verifyNoInteractions(gateway, escrowService);
    }

    private BookingSnapshot acceptedBooking() {
        return BookingSnapshot.builder()
                .bookingId(7L)
                .buyerId(10L)
                .sellerId(20L)
                .amount(new BigDecimal("10000.00"))
                .platformFee(new BigDecimal("200.00"))
                .sellerAmount(new BigDecimal("9800.00"))
                .bookingStatus(BookingStatus.ACCEPTED)
                .build();
    }

    private Payment payment(PaymentStatus status) {
        return Payment.builder()
                .bookingId(7L)
                .buyerId(10L)
                .sellerId(20L)
                .transactionId("TXN-1")
                .amount(new BigDecimal("10000.00"))
                .currency("INR")
                .paymentMethod(PaymentMethod.UNKNOWN)
                .paymentStatus(status)
                .build();
    }
}
