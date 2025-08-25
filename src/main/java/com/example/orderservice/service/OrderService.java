
package com.example.orderservice.service;

import com.example.orderservice.domain.ExportOutbox;
import com.example.orderservice.domain.Order;
import com.example.orderservice.domain.OrderStatus;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.repository.ExportOutboxRepository;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;

@Service @RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ExportOutboxRepository outboxRepository;
    private final ExternalBClient externalBClient;

    @Transactional
    public Mono<OrderResponse> createOrUpdate(OrderRequest request) {
        return orderRepository.findByExternalOrderId(request.getExternalOrderId())
                .defaultIfEmpty(Order.builder().externalOrderId(request.getExternalOrderId()).build())
                .flatMap(existing -> {
                    existing.setCustomerId(request.getCustomerId());
                    existing.setItems(request.getItems());
                    existing.setTotal(CalculationUtils.totalOf(request.getItems()));
                    existing.setStatus(OrderStatus.PROCESSED);
                    return orderRepository.save(existing);
                })
                .flatMap(saved -> exportToB(saved)
                        .thenReturn(saved)
                        .onErrorResume(ex -> outboxRepository.save(ExportOutbox.builder()
                                        .orderId(saved.getId())
                                        .attempts(0)
                                        .nextAttemptAt(Instant.now().plusSeconds(30))
                                        .lastError(ex.getMessage())
                                        .build())
                                .thenReturn(saved)))
                .map(this::toResponse);
    }

    public Mono<OrderResponse> getById(String id) {
        return orderRepository.findById(id).map(this::toResponse);
    }

    public Flux<OrderResponse> list() {
        return orderRepository.findAll().map(this::toResponse);
    }

    public Mono<Void> exportToB(Order order) {
        return externalBClient.exportOrder(order)
                .doOnSuccess(v -> order.setStatus(OrderStatus.EXPORTED))
                .then(orderRepository.save(order)).then();
    }

    private OrderResponse toResponse(Order o) {
        return OrderResponse.builder()
                .id(o.getId())
                .externalOrderId(o.getExternalOrderId())
                .customerId(o.getCustomerId())
                .items(o.getItems())
                .total(o.getTotal() == null ? BigDecimal.ZERO : o.getTotal())
                .status(o.getStatus())
                .build();
    }
}
