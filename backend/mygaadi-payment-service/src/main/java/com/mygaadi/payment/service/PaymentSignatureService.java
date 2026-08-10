package com.mygaadi.payment.service;

import com.mygaadi.payment.common.ApiException;
import com.mygaadi.payment.config.RazorpayConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class PaymentSignatureService {
    private final RazorpayConfig razorpayConfig;

    public boolean verifyPayment(String orderId, String paymentId, String signature) {
        return verify(orderId + "|" + paymentId, signature, razorpayConfig.getKeySecret());
    }

    public boolean verifyWebhook(String payload, String signature) {
        return verify(payload, signature, razorpayConfig.getWebhookSecret());
    }

    private boolean verify(String payload, String signature, String secret) {
        if (payload == null || signature == null || secret == null || secret.isBlank()) return false;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] expected = HexFormat.of().formatHex(
                    mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)))
                    .getBytes(StandardCharsets.US_ASCII);
            byte[] actual = signature.trim().toLowerCase().getBytes(StandardCharsets.US_ASCII);
            return MessageDigest.isEqual(expected, actual);
        } catch (Exception ex) {
            throw ApiException.badRequest("Payment signature verification could not be completed");
        }
    }
}
