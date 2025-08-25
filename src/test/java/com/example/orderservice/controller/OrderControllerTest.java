
package com.example.orderservice.controller;

import com.example.orderservice.domain.OrderItem;
import com.example.orderservice.domain.OrderStatus;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.service.ExternalAClient;
import com.example.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

@WebFluxTest(controllers = OrderController.class)
class OrderControllerTest {

    @Autowired WebTestClient webTestClient;

    @MockBean OrderService orderService;
    @MockBean ExternalAClient externalAClient;

    @Test
    void create_shouldReturn201() {
        var req = OrderRequest.builder()
                .externalOrderId("A-1").customerId("C-1")
                .items(List.of(OrderItem.builder().productId("P1").name("A").price(new BigDecimal("10")).quantity(1).build()))
                .build();

        var resp = OrderResponse.builder()
                .id("1").externalOrderId("A-1").customerId("C-1")
                .items(req.getItems()).total(new BigDecimal("10")).status(OrderStatus.PROCESSED)
                .build();

        Mockito.when(orderService.createOrUpdate(any(OrderRequest.class))).thenReturn(Mono.just(resp));

        webTestClient.post().uri("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("1")
                .jsonPath("$.externalOrderId").isEqualTo("A-1");
    }

    @Test
    void list_shouldReturnOk() {
        Mockito.when(orderService.list()).thenReturn(Flux.just(
                OrderResponse.builder().id("1").externalOrderId("A-1").build()
        ));
        webTestClient.get().uri("/api/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("1");
    }
}
