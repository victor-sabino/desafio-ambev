
package com.example.orderservice.service;

import com.example.orderservice.dto.OrderRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Service @RequiredArgsConstructor
public class ExternalAClient {
    private final WebClient externalAWebClient;

    @CircuitBreaker(name = "externalA")
    @Retry(name = "externalA")
    @TimeLimiter(name = "externalA")
    public Flux<OrderRequest> fetchPendingOrders() {
        return externalAWebClient.get()
                .uri("/orders/pending")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(OrderRequest.class);
    }
}
