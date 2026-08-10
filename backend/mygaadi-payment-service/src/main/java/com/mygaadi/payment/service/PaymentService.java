package com.mygaadi.payment.service;

import com.mygaadi.payment.common.ApiException;
import com.mygaadi.payment.config.RazorpayConfig;
import com.mygaadi.payment.dto.*;
import com.mygaadi.payment.entity.Payment;
import com.mygaadi.payment.enums.*;
import com.mygaadi.payment.repository.PaymentEventRepository;
import com.mygaadi.payment.repository.PaymentRepository;
import com.mygaadi.payment.security.JwtPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentEventRepository eventRepository;
    private final BookingClient bookingClient;
    private final RazorpayGateway razorpayGateway;
    private final RazorpayConfig razorpayConfig;
    private final PaymentSignatureService signatureService;
    private final EscrowService escrowService;
    private final AuditService auditService;
    private final OwnershipService ownershipService;

    @Transactional
    public CreateOrderResponse createOrder(Long bookingId, String idempotencyKey,
                                           String authorizationHeader, JwtPrincipal buyer) {
        if (buyer.getRole() != UserRole.BUYER) throw ApiException.forbidden("Buyer role required");

        BookingSnapshot booking = bookingClient.getBooking(bookingId, authorizationHeader);
        validateBookingForPayment(booking, buyer);

        Payment existing = paymentRepository.findByBookingIdForUpdate(bookingId).orElse(null);
        if (existing != null) {
            ownershipService.requireBuyer(buyer, existing.getBuyerId());
            if (existing.getPaymentStatus() == PaymentStatus.SUCCESS) {
                throw ApiException.conflict("Payment already completed for this booking");
            }
            if (existing.getPaymentStatus() == PaymentStatus.REFUND_PENDING
                    || existing.getPaymentStatus() == PaymentStatus.REFUNDED) {
                throw ApiException.conflict("This booking payment is already in a refund flow");
            }
            if (existing.getRazorpayOrderId() != null
                    && (existing.getPaymentStatus() == PaymentStatus.ORDER_CREATED
                    || existing.getPaymentStatus() == PaymentStatus.AUTHORIZED
                    || existing.getPaymentStatus() == PaymentStatus.PENDING)) {
                return toOrderResponse(existing, true);
            }
        }

        Payment payment = existing == null ? new Payment() : existing;
        payment.setBookingId(booking.getBookingId());
        payment.setBuyerId(booking.getBuyerId());
        payment.setSellerId(booking.getSellerId());
        payment.setTransactionId(transactionId(bookingId, idempotencyKey));
        payment.setAmount(booking.getAmount());
        payment.setCurrency(razorpayConfig.getCurrency());
        payment.setPaymentMethod(PaymentMethod.UNKNOWN);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setFailureReason(null);
        payment.setRazorpayOrderId(null);
        payment.setRazorpayPaymentId(null);
        payment.setRazorpaySignature(null);
        payment.setPaidAt(null);
        paymentRepository.saveAndFlush(payment);

        RazorpayGateway.GatewayOrder gatewayOrder = razorpayGateway.createOrder(
                booking.getBookingId(), booking.getBuyerId(), booking.getSellerId(),
                booking.getAmount(), razorpayConfig.getCurrency());

        validateGatewayOrder(gatewayOrder, booking.getAmount());
        payment.setRazorpayOrderId(gatewayOrder.orderId());
        payment.setPaymentStatus(PaymentStatus.ORDER_CREATED);
        payment.setGatewayResponse(orderContext(gatewayOrder, booking));
        auditService.recordInternal(payment, "ORDER_CREATED", "{\"bookingId\":" + bookingId + "}");
        return toOrderResponse(payment, false);
    }

    @Transactional
    public PaymentResponse verifyPayment(Long bookingId, VerifyPaymentRequest request, JwtPrincipal buyer) {
        Payment payment = paymentRepository.findByBookingIdForUpdate(bookingId)
                .orElseThrow(() -> ApiException.notFound("Payment order not found"));
        ownershipService.requireBuyer(buyer, payment.getBuyerId());

        if (!request.getRazorpayOrderId().equals(payment.getRazorpayOrderId())) {
            throw ApiException.badRequest("Razorpay order does not belong to this booking");
        }
        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            if (request.getRazorpayPaymentId().equals(payment.getRazorpayPaymentId())) {
                return response(payment);
            }
            throw ApiException.conflict("A different payment is already completed for this booking");
        }
        if (payment.getPaymentStatus() == PaymentStatus.REFUND_PENDING
                || payment.getPaymentStatus() == PaymentStatus.REFUNDED) {
            throw ApiException.conflict("Refunded payment cannot be verified again");
        }

        if (!signatureService.verifyPayment(request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(), request.getRazorpaySignature())) {
            throw ApiException.badRequest("Invalid Razorpay signature");
        }

        RazorpayGateway.GatewayPayment gatewayPayment = razorpayGateway.fetchPayment(request.getRazorpayPaymentId());
        validateGatewayPayment(payment, gatewayPayment);

        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setPaymentMethod(toPaymentMethod(gatewayPayment.method()));
        BigDecimal platformFee = contextAmount(payment.getGatewayResponse(), "platformFee");
        BigDecimal sellerAmount = contextAmount(payment.getGatewayResponse(), "sellerAmount");

        if ("authorized".equalsIgnoreCase(gatewayPayment.status())) {
            payment.setPaymentStatus(PaymentStatus.AUTHORIZED);
            auditService.recordInternal(payment, "PAYMENT_AUTHORIZED", "{}");
            return response(payment);
        }
        if (!"captured".equalsIgnoreCase(gatewayPayment.status())) {
            throw ApiException.badRequest("Payment is not captured");
        }

        payment.setGatewayResponse(safePaymentSummary(gatewayPayment));
        markSuccessful(payment, Instant.now(), "CLIENT_VERIFIED");
        escrowService.holdEscrow(payment, bookingId, platformFee, sellerAmount);
        return response(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getByBooking(Long bookingId, JwtPrincipal principal) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> ApiException.notFound("Payment not found"));
        ownershipService.requireParticipantOrAdmin(principal, payment);
        return response(payment);
    }

    public void markSuccessful(Payment payment, Instant paidAt, String source) {
        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) return;
        if (payment.getPaymentStatus() == PaymentStatus.REFUNDED
                || payment.getPaymentStatus() == PaymentStatus.REFUND_PENDING) {
            throw ApiException.conflict("Refunded payment cannot become successful");
        }
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setFailureReason(null);
        payment.setPaidAt(paidAt == null ? Instant.now() : paidAt);
        auditService.recordInternal(payment, "PAYMENT_SUCCESS", "{\"source\":\"" + safe(source) + "\"}");
    }

    private void validateBookingForPayment(BookingSnapshot booking, JwtPrincipal buyer) {
        if (booking.getBookingId() == null || booking.getBuyerId() == null || booking.getSellerId() == null) {
            throw ApiException.serviceUnavailable("Booking response is incomplete");
        }
        ownershipService.requireBuyer(buyer, booking.getBuyerId());
        if (booking.getBookingStatus() != BookingStatus.ACCEPTED) {
            throw ApiException.badRequest("Payment is allowed only after the seller accepts the booking");
        }
        if (booking.getAmount() == null || booking.getAmount().signum() <= 0) {
            throw ApiException.badRequest("Booking amount is invalid");
        }
        if (booking.getPlatformFee() == null || booking.getSellerAmount() == null
                || booking.getPlatformFee().add(booking.getSellerAmount()).compareTo(booking.getAmount()) != 0) {
            throw ApiException.badRequest("Booking amount breakdown is invalid");
        }
    }

    private void validateGatewayOrder(RazorpayGateway.GatewayOrder order, BigDecimal amount) {
        long expected = toPaise(amount);
        if (order.orderId() == null || order.amountInPaise() != expected
                || !razorpayConfig.getCurrency().equalsIgnoreCase(order.currency())) {
            throw ApiException.badRequest("Razorpay order amount or currency mismatch");
        }
    }

    private void validateGatewayPayment(Payment payment, RazorpayGateway.GatewayPayment gatewayPayment) {
        if (!payment.getRazorpayOrderId().equals(gatewayPayment.orderId())) {
            throw ApiException.badRequest("Razorpay payment belongs to a different order");
        }
        if (gatewayPayment.amountInPaise() != toPaise(payment.getAmount())) {
            throw ApiException.badRequest("Razorpay payment amount mismatch");
        }
        if (!payment.getCurrency().equalsIgnoreCase(gatewayPayment.currency())) {
            throw ApiException.badRequest("Razorpay payment currency mismatch");
        }
    }

    private CreateOrderResponse toOrderResponse(Payment payment, boolean reused) {
        return CreateOrderResponse.builder()
                .bookingId(payment.getBookingId())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .keyId(razorpayConfig.getKeyId())
                .amount(toPaise(payment.getAmount()))
                .currency(payment.getCurrency())
                .name("MyGaadi.com")
                .description("Used car booking payment")
                .reused(reused)
                .build();
    }

    private PaymentResponse response(Payment payment) {
        List<PaymentEventResponse> events = eventRepository.findByPaymentIdOrderByCreatedAtAsc(payment.getId())
                .stream().map(PaymentEventResponse::from).toList();
        return PaymentResponse.from(payment, events);
    }

    private long toPaise(BigDecimal amount) {
        return amount.movePointRight(2).longValueExact();
    }

    private String transactionId(Long bookingId, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) return "TXN-" + UUID.randomUUID();
        if (idempotencyKey.length() > 128) throw ApiException.badRequest("Idempotency-Key is too long");
        try {
            String input = bookingId + ":" + idempotencyKey.trim();
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
            return "IDEMP-" + HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            throw ApiException.badRequest("Idempotency key could not be processed");
        }
    }

    private PaymentMethod toPaymentMethod(String method) {
        if (method == null) return PaymentMethod.UNKNOWN;
        return switch (method.toLowerCase(Locale.ROOT)) {
            case "upi" -> PaymentMethod.UPI;
            case "card" -> PaymentMethod.CARD;
            case "netbanking" -> PaymentMethod.NET_BANKING;
            case "wallet" -> PaymentMethod.WALLET;
            default -> PaymentMethod.UNKNOWN;
        };
    }


    private String orderContext(RazorpayGateway.GatewayOrder order, BookingSnapshot booking) {
        return "{\"orderId\":\"" + safe(order.orderId())
                + "\",\"status\":\"created\",\"platformFee\":\""
                + booking.getPlatformFee().toPlainString()
                + "\",\"sellerAmount\":\"" + booking.getSellerAmount().toPlainString() + "\"}";
    }

    public BigDecimal contextAmount(String gatewayResponse, String field) {
        try {
            org.json.JSONObject json = new org.json.JSONObject(gatewayResponse);
            return new BigDecimal(json.getString(field));
        } catch (Exception ex) {
            throw ApiException.badRequest("Trusted booking amount breakdown is unavailable");
        }
    }

    private String safePaymentSummary(RazorpayGateway.GatewayPayment payment) {
        return "{\"paymentId\":\"" + safe(payment.paymentId()) + "\",\"orderId\":\""
                + safe(payment.orderId()) + "\",\"status\":\"" + safe(payment.status()) + "\"}";
    }

    private String safe(String value) {
        return value == null ? "" : value.replace("\\", "").replace("\"", "");
    }
}
