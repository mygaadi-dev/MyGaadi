package com.mygaadi.payment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class BookingClientConfig {
    @Bean
    public RestTemplate bookingRestTemplate(
            RestTemplateBuilder builder,
            @Value("${booking.client.connect-timeout-ms}") long connectTimeoutMs,
            @Value("${booking.client.read-timeout-ms}") long readTimeoutMs) {
        return builder
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .readTimeout(Duration.ofMillis(readTimeoutMs))
                .build();
    }
}
