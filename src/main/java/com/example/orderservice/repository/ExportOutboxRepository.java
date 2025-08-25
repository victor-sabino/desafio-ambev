
package com.example.orderservice.repository;

import com.example.orderservice.domain.ExportOutbox;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import java.time.Instant;

public interface ExportOutboxRepository extends ReactiveMongoRepository<ExportOutbox, String> {
    Flux<ExportOutbox> findByNextAttemptAtBefore(Instant instant);
}
