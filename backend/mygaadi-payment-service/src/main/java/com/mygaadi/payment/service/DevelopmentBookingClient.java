package com.mygaadi.payment.service;

import com.mygaadi.payment.common.ApiException;
import com.mygaadi.payment.common.ApiResponse;
import com.mygaadi.payment.dto.BookingSnapshot;
import com.mygaadi.payment.dto.ExistingBookingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

/**
 * Temporary adapter for the current MyGaadi monolith. It forwards the buyer JWT to
 * GET /api/bookings/buyer/me and selects the requested booking. Replace this class
 * with an internal booking endpoint client when that endpoint becomes available.
 */
@Service
@RequiredArgsConstructor
public class DevelopmentBookingClient implements BookingClient {
    private final RestTemplate bookingRestTemplate;

    @Value("${booking.service.base-url}")
    private String baseUrl;

    @Override
    public BookingSnapshot getBooking(Long bookingId, String authorizationHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<ApiResponse<List<ExistingBookingResponse>>> response = bookingRestTemplate.exchange(
                    baseUrl + "/api/bookings/buyer/me",
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<>() { }
            );
            List<ExistingBookingResponse> bookings = response.getBody() == null || response.getBody().getData() == null
                    ? Collections.emptyList()
                    : response.getBody().getData();
            return bookings.stream()
                    .filter(booking -> bookingId.equals(booking.getId()))
                    .findFirst()
                    .map(ExistingBookingResponse::toSnapshot)
                    .orElseThrow(() -> ApiException.notFound("Booking not found"));
        } catch (ApiException ex) {
            throw ex;
        } catch (ResourceAccessException ex) {
            throw ApiException.serviceUnavailable("Booking service is unavailable");
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 401) {
                throw ApiException.unauthorized("Booking service rejected the access token");
            }
            if (ex.getStatusCode().value() == 403) {
                throw ApiException.forbidden("Booking access denied");
            }
            throw ApiException.serviceUnavailable("Booking verification could not be completed");
        }
    }
}
