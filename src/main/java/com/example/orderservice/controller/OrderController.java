
package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.service.ExternalAClient;
import com.example.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final ExternalAClient externalAClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<OrderResponse> create(@Valid @RequestBody OrderRequest req) {
        return orderService.createOrUpdate(req);
    }

    @GetMapping("")
    public Flux<OrderResponse> list() {
        return orderService.list();
    }

    @GetMapping("/{id}")
    public Mono<OrderResponse> get(@PathVariable String id) {
        return orderService.getById(id);
    }

    @PostMapping("/integrations/a/pull")
    public Flux<OrderResponse> pullFromA() {
        return externalAClient.fetchPendingOrders()
                .flatMap(orderService::createOrUpdate);
    }
}
