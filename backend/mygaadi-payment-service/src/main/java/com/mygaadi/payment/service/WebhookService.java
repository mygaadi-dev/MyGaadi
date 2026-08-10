package com.mygaadi.payment.service;

import com.mygaadi.payment.common.ApiException;
import com.mygaadi.payment.dto.WebhookResponse;
import com.mygaadi.payment.entity.Escrow;
import com.mygaadi.payment.entity.Payment;
import com.mygaadi.payment.enums.EscrowStatus;
import com.mygaadi.payment.enums.PaymentMethod;
import com.mygaadi.payment.enums.PaymentStatus;
import com.mygaadi.payment.repository.EscrowRepository;
import com.mygaadi.payment.repository.PaymentEventRepository;
import com.mygaadi.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class WebhookService {
    private final PaymentSignatureService signatureService;
    private final PaymentRepository paymentRepository;
    private final EscrowRepository escrowRepository;
    private final PaymentEventRepository eventRepository;
    private final PaymentService paymentService;
    private final EscrowService escrowService;

    @Transactional
    public WebhookResponse process(String payload, String signature, String suppliedEventId) {
        if (!signatureService.verifyWebhook(payload, signature)) {
            throw ApiException.badRequest("Invalid Razorpay webhook signature");
        }

        JSONObject root;
        try {
            root = new JSONObject(payload);
        } catch (Exception ex) {
            throw ApiException.badRequest("Invalid webhook payload");
        }

        String eventType = root.optString("event", "").trim();
        if (eventType.isBlank()) throw ApiException.badRequest("Webhook event type is missing");
        String eventId = eventId(suppliedEventId, signature, payload);

        if (!isSupported(eventType)) {
            return WebhookResponse.builder()
                    .eventId(eventId)
                    .eventType(eventType)
                    .duplicate(false)
                    .processed(false)
                    .build();
        }

        JSONObject entity = eventEntity(root, eventType);
        Payment payment = findPayment(entity, eventType);
        String safePayload = safeSummary(eventType, entity);

        int inserted = eventRepository.reserveGatewayEvent(
                payment.getId(), eventId, eventType, safePayload);
        if (inserted == 0) {
            return WebhookResponse.builder()
                    .eventId(eventId)
                    .eventType(eventType)
                    .duplicate(true)
                    .processed(false)
                    .build();
        }

        switch (eventType) {
            case "payment.captured", "order.paid" -> handleCaptured(payment, entity);
            case "payment.authorized" -> handleAuthorized(payment, entity);
            case "payment.failed" -> handleFailed(payment, entity);
            case "refund.processed" -> handleRefundProcessed(payment);
            case "refund.failed" -> handleRefundFailed(payment, entity);
            default -> { }
        }

        return WebhookResponse.builder()
                .eventId(eventId)
                .eventType(eventType)
                .duplicate(false)
                .processed(true)
                .build();
    }

    private void handleCaptured(Payment payment, JSONObject entity) {
        String paymentId = entity.optString("id", null);
        String orderId = entity.optString("order_id", null);
        long amount = entity.optLong("amount", -1);
        String currency = entity.optString("currency", "");

        validatePaymentIdentity(payment, orderId, amount, currency);
        if (payment.getRazorpayPaymentId() != null
                && !payment.getRazorpayPaymentId().equals(paymentId)) {
            throw ApiException.conflict("A different payment is already linked to this booking");
        }

        BigDecimal platformFee = null;
        BigDecimal sellerAmount = null;
        if (escrowRepository.findByPaymentId(payment.getId()).isEmpty()) {
            platformFee = paymentService.contextAmount(payment.getGatewayResponse(), "platformFee");
            sellerAmount = paymentService.contextAmount(payment.getGatewayResponse(), "sellerAmount");
        }

        payment.setRazorpayPaymentId(paymentId);
        payment.setPaymentMethod(toPaymentMethod(entity.optString("method", null)));
        payment.setGatewayResponse(safeSummary("payment.captured", entity));
        paymentService.markSuccessful(payment, epochSeconds(entity.optLong("created_at", 0)), "RAZORPAY_WEBHOOK");

        if (escrowRepository.findByPaymentId(payment.getId()).isEmpty()) {
            escrowService.holdEscrow(payment, payment.getBookingId(), platformFee, sellerAmount);
        }
    }

    private void handleAuthorized(Payment payment, JSONObject entity) {
        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) return;
        validatePaymentIdentity(payment,
                entity.optString("order_id", null),
                entity.optLong("amount", -1),
                entity.optString("currency", ""));
        payment.setRazorpayPaymentId(entity.optString("id", null));
        payment.setPaymentMethod(toPaymentMethod(entity.optString("method", null)));
        payment.setPaymentStatus(PaymentStatus.AUTHORIZED);
    }

    private void handleFailed(Payment payment, JSONObject entity) {
        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS
                || payment.getPaymentStatus() == PaymentStatus.REFUND_PENDING
                || payment.getPaymentStatus() == PaymentStatus.REFUNDED) return;
        payment.setRazorpayPaymentId(emptyToNull(entity.optString("id", null)));
        payment.setPaymentStatus(PaymentStatus.FAILED);
        payment.setFailureReason(firstNonBlank(
                entity.optString("error_description", null),
                entity.optString("error_reason", null),
                "Razorpay payment failed"));
        payment.setGatewayResponse(safeSummary("payment.failed", entity));
    }

    private void handleRefundProcessed(Payment payment) {
        Escrow escrow = escrowRepository.findByPaymentIdForUpdate(payment.getId())
                .orElseThrow(() -> ApiException.notFound("Escrow not found for refunded payment"));
        escrowService.completeRefund(payment, escrow, "RAZORPAY_WEBHOOK");
    }

    private void handleRefundFailed(Payment payment, JSONObject entity) {
        Escrow escrow = escrowRepository.findByPaymentIdForUpdate(payment.getId())
                .orElseThrow(() -> ApiException.notFound("Escrow not found for refund failure"));
        if (escrow.getEscrowStatus() == EscrowStatus.REFUND_PENDING) {
            escrow.setEscrowStatus(EscrowStatus.HELD);
        }
        if (payment.getPaymentStatus() == PaymentStatus.REFUND_PENDING) {
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
        }
        payment.setFailureReason(firstNonBlank(
                entity.optString("error_description", null),
                "Razorpay refund failed"));
        payment.setGatewayResponse(safeSummary("refund.failed", entity));
    }

    private Payment findPayment(JSONObject entity, String eventType) {
        String paymentId = eventType.startsWith("refund.")
                ? emptyToNull(entity.optString("payment_id", null))
                : emptyToNull(entity.optString("id", null));
        String orderId = emptyToNull(entity.optString("order_id", null));

        if (paymentId != null) {
            Payment byPaymentId = paymentRepository.findByRazorpayPaymentId(paymentId).orElse(null);
            if (byPaymentId != null) return byPaymentId;
        }
        if (orderId != null) {
            return paymentRepository.findByRazorpayOrderId(orderId)
                    .orElseThrow(() -> ApiException.notFound("Payment order not found for webhook"));
        }
        throw ApiException.badRequest("Webhook payment reference is missing");
    }

    private JSONObject eventEntity(JSONObject root, String eventType) {
        JSONObject payload = root.optJSONObject("payload");
        if (payload == null) throw ApiException.badRequest("Webhook payload section is missing");
        String section = eventType.startsWith("refund.") ? "refund" : "payment";
        JSONObject wrapper = payload.optJSONObject(section);
        if (wrapper == null || wrapper.optJSONObject("entity") == null) {
            throw ApiException.badRequest("Webhook entity is missing");
        }
        return wrapper.getJSONObject("entity");
    }

    private void validatePaymentIdentity(Payment payment, String orderId,
                                         long amountInPaise, String currency) {
        if (orderId == null || !orderId.equals(payment.getRazorpayOrderId())) {
            throw ApiException.badRequest("Webhook order ID mismatch");
        }
        if (amountInPaise != payment.getAmount().movePointRight(2).longValueExact()) {
            throw ApiException.badRequest("Webhook payment amount mismatch");
        }
        if (!payment.getCurrency().equalsIgnoreCase(currency)) {
            throw ApiException.badRequest("Webhook payment currency mismatch");
        }
    }

    private boolean isSupported(String eventType) {
        return switch (eventType) {
            case "payment.captured", "payment.authorized", "payment.failed",
                    "order.paid", "refund.processed", "refund.failed" -> true;
            default -> false;
        };
    }

    private String eventId(String suppliedEventId, String signature, String payload) {
        if (suppliedEventId != null && !suppliedEventId.isBlank()) {
            return suppliedEventId.trim().substring(0, Math.min(160, suppliedEventId.trim().length()));
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((signature + "|" + payload).getBytes(StandardCharsets.UTF_8));
            return "derived:" + HexFormat.of().formatHex(digest);
        } catch (Exception ex) {
            throw ApiException.badRequest("Webhook event ID could not be generated");
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

    private String safeSummary(String eventType, JSONObject entity) {
        return new JSONObject()
                .put("event", eventType)
                .put("id", entity.optString("id", ""))
                .put("paymentId", entity.optString("payment_id", ""))
                .put("orderId", entity.optString("order_id", ""))
                .put("status", entity.optString("status", ""))
                .put("amount", entity.optLong("amount", 0))
                .put("currency", entity.optString("currency", ""))
                .toString();
    }

    private Instant epochSeconds(long value) {
        return value > 0 ? Instant.ofEpochSecond(value) : Instant.now();
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private String firstNonBlank(String first, String second, String fallback) {
        if (first != null && !first.isBlank()) return first;
        if (second != null && !second.isBlank()) return second;
        return fallback;
    }

    private String firstNonBlank(String first, String fallback) {
        return firstNonBlank(first, null, fallback);
    }
}
