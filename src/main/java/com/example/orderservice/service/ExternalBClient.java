
package com.example.orderservice.service;

import com.example.orderservice.domain.Order;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service @RequiredArgsConstructor
public class ExternalBClient {
    private final WebClient externalBWebClient;

    @CircuitBreaker(name = "externalB")
    @Retry(name = "externalB")
    @TimeLimiter(name = "externalB")
    @RateLimiter(name = "exportB")
    @Bulkhead(name = "externalB")
    public Mono<Void> exportOrder(Order order) {
        return externalBWebClient.post()
                .uri("/orders/processed")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "orderId", order.getId(),
                        "externalOrderId", order.getExternalOrderId(),
                        "total", order.getTotal(),
                        "status", order.getStatus().name()))
                .retrieve()
                .bodyToMono(Void.class);
    }
}
