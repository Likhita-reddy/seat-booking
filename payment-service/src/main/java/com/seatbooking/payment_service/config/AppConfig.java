package com.seatbooking.payment_service.config;

import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(WebhookProperties.class)
public class AppConfig {

    private final Tracer tracer;
    private final Propagator propagator;

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(List.of(
                (request, body, execution) -> {
                    if (tracer.currentSpan() != null) {
                        propagator.inject(
                                tracer.currentSpan().context(),
                                request.getHeaders(),
                                (headers, key, value) -> headers.set(key, value)
                        );
                    }
                    return execution.execute(request, body);
                }
        ));
        return restTemplate;
    }
}