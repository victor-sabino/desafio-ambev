
package com.example.orderservice.service;

import com.example.orderservice.domain.Order;
import com.example.orderservice.domain.OrderItem;
import com.example.orderservice.domain.OrderStatus;
import com.example.orderservice.dto.OrderRequest;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ExternalClientsTest {

    private WireMockServer server;

    @BeforeEach
    void start() {
        server = new WireMockServer(0);
        server.start();
        configureFor("localhost", server.port());
    }

    @AfterEach
    void stop() {
        server.stop();
    }

    @Test
    void externalA_fetchPendingOrders() {
        server.stubFor(get(urlEqualTo("/orders/pending"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\n" +
                                "  \"externalOrderId\": \"A-100\",\n" +
                                "  \"customerId\": \"C-9\",\n" +
                                "  \"items\":[{\"productId\":\"P1\",\"name\":\"X\",\"price\":10,\"quantity\":1}]\n" +
                                "}]")));

        var client = new ExternalAClient(WebClient.builder().baseUrl("http://localhost:"+server.port()).build());

        Flux<OrderRequest> flux = client.fetchPendingOrders();
        StepVerifier.create(flux)
                .assertNext(or -> {
                    assertEquals("A-100", or.getExternalOrderId());
                    assertEquals("C-9", or.getCustomerId());
                })
                .verifyComplete();
    }

    @Test
    void externalB_exportOrder() {
        server.stubFor(post(urlEqualTo("/orders/processed"))
                .willReturn(aResponse().withStatus(202)));

        var client = new ExternalBClient(WebClient.builder().baseUrl("http://localhost:"+server.port()).build());

        Order order = Order.builder()
                .id("1")
                .externalOrderId("A-200")
                .status(OrderStatus.PROCESSED)
                .items(List.of(OrderItem.builder().productId("P1").name("X").price(new BigDecimal("9.9")).quantity(1).build()))
                .total(new BigDecimal("9.9"))
                .build();

        Mono<Void> result = client.exportOrder(order);
        StepVerifier.create(result).verifyComplete();

        server.verify(postRequestedFor(urlEqualTo("/orders/processed")));
    }
}
