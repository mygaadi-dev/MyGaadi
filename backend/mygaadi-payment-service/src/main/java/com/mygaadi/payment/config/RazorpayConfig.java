package com.mygaadi.payment.config;

import com.razorpay.RazorpayClient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "razorpay")
public class RazorpayConfig {
    private String keyId;
    private String keySecret;
    private String webhookSecret;
    private String currency = "INR";

    @Bean
    public RazorpayClient razorpayClient() throws Exception {
        return new RazorpayClient(keyId, keySecret);
    }
}
