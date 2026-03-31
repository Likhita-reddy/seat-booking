package com.seatbooking.payment_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix="booking.service.webhook")
@Data
public class WebhookProperties {
    private String url;
    private int maxRetries;
    private Long initialBackoffMs;
}
