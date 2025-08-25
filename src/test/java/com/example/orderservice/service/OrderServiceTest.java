
package com.example.orderservice.service;

import com.example.orderservice.domain.ExportOutbox;
import com.example.orderservice.domain.Order;
import com.example.orderservice.domain.OrderItem;
import com.example.orderservice.domain.OrderStatus;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.repository.ExportOutboxRepository;
import com.example.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock ExportOutboxRepository outboxRepository;
    @Mock ExternalBClient externalBClient;
    @InjectMocks OrderService orderService;

    OrderRequest request;

    @BeforeEach
    void setup() {
        request = OrderRequest.builder()
                .externalOrderId("A-1")
                .customerId("C-1")
                .items(List.of(
                        OrderItem.builder().productId("P1").name("A").price(new BigDecimal("10")).quantity(2).build(),
                        OrderItem.builder().productId("P2").name("B").price(new BigDecimal("5")).quantity(1).build()
                ))
                .build();
    }

    @Test
    void createOrUpdate_shouldSaveAndExport_whenExternalBUp() {
        when(orderRepository.findByExternalOrderId("A-1")).thenReturn(Mono.empty());

        Order saved = Order.builder()
                .id("1").externalOrderId("A-1").customerId("C-1")
                .items(request.getItems())
                .total(new BigDecimal("25"))
                .status(OrderStatus.PROCESSED)
                .build();
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(saved));
        when(externalBClient.exportOrder(any(Order.class))).thenReturn(Mono.empty());

        StepVerifier.create(orderService.createOrUpdate(request))
                .expectNextMatches(r -> r.getTotal().compareTo(new BigDecimal("25")) == 0
                        && "A-1".equals(r.getExternalOrderId()))
                .verifyComplete();

        verify(orderRepository, atLeastOnce()).save(any(Order.class));
        verify(externalBClient).exportOrder(any(Order.class));
        verify(outboxRepository, never()).save(any(ExportOutbox.class));
    }

    @Test
    void createOrUpdate_shouldCreateOutbox_whenExportFails() {
        when(orderRepository.findByExternalOrderId("A-1")).thenReturn(Mono.empty());

        Order saved = Order.builder()
                .id("1").externalOrderId("A-1").customerId("C-1")
                .items(request.getItems())
                .total(new BigDecimal("25"))
                .status(OrderStatus.PROCESSED)
                .build();
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(saved));
        when(externalBClient.exportOrder(any(Order.class))).thenReturn(Mono.error(new RuntimeException("B down")));
        when(outboxRepository.save(any(ExportOutbox.class))).thenReturn(Mono.just(ExportOutbox.builder().id("o1").build()));

        StepVerifier.create(orderService.createOrUpdate(request))
                .expectNextMatches(r -> "A-1".equals(r.getExternalOrderId()))
                .verifyComplete();

        verify(outboxRepository).save(any(ExportOutbox.class));
    }
}
