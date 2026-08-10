package com.mygaadi.payment.service;

import com.mygaadi.payment.config.RazorpayConfig;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

class PaymentSignatureServiceTest {
    @Test
    void verifiesValidPaymentAndWebhookSignatures() throws Exception {
        RazorpayConfig config = new RazorpayConfig();
        config.setKeySecret("payment-secret");
        config.setWebhookSecret("webhook-secret");
        PaymentSignatureService service = new PaymentSignatureService(config);

        String paymentPayload = "order_123|pay_123";
        String webhookPayload = "{\"event\":\"payment.captured\"}";

        assertTrue(service.verifyPayment("order_123", "pay_123", hmac(paymentPayload, "payment-secret")));
        assertTrue(service.verifyWebhook(webhookPayload, hmac(webhookPayload, "webhook-secret")));
    }

    @Test
    void rejectsInvalidSignature() {
        RazorpayConfig config = new RazorpayConfig();
        config.setKeySecret("payment-secret");
        PaymentSignatureService service = new PaymentSignatureService(config);

        assertFalse(service.verifyPayment("order_123", "pay_123", "invalid"));
    }

    private String hmac(String payload, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    }
}
