package com.hackathon.payment.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

/**
 * RetryTemplate used for gateway calls: 3 attempts with exponential backoff,
 * retrying only technical failures ({@link PaymentGatewayException}).
 */
@Configuration
public class GatewayRetryConfig {

    @Bean
    public RetryTemplate gatewayRetryTemplate(
            @Value("${app.gateway.retry.max-attempts:3}") int maxAttempts,
            @Value("${app.gateway.retry.initial-interval-ms:200}") long initialIntervalMs,
            @Value("${app.gateway.retry.multiplier:2.0}") double multiplier) {
        return RetryTemplate.builder()
                .maxAttempts(maxAttempts)
                .exponentialBackoff(initialIntervalMs, multiplier, initialIntervalMs * 10)
                .retryOn(PaymentGatewayException.class)
                .build();
    }
}
