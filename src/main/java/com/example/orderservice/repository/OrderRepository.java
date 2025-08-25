
package com.example.orderservice.repository;

import com.example.orderservice.domain.Order;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface OrderRepository extends ReactiveMongoRepository<Order, String> {
    Mono<Order> findByExternalOrderId(String externalOrderId);
}
