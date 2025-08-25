
package com.example.orderservice.jobs;

import com.example.orderservice.repository.ExportOutboxRepository;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.service.ExternalBClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxRetryJob {
    private final ExportOutboxRepository outboxRepository;
    private final OrderRepository orderRepository;
    private final ExternalBClient externalBClient;

    @Scheduled(fixedDelayString = "PT30S")
    public void retry() {
        outboxRepository.findByNextAttemptAtBefore(Instant.now())
                .flatMap(entry -> orderRepository.findById(entry.getOrderId())
                        .flatMap(order -> externalBClient.exportOrder(order)
                                .then(Mono.fromRunnable(() -> log.info("Exported order {} via retry", order.getId())))
                                .then(outboxRepository.deleteById(entry.getId())))
                        .onErrorResume(ex -> {
                            entry.setAttempts(entry.getAttempts() + 1);
                            entry.setLastError(ex.getMessage());
                            entry.setNextAttemptAt(Instant.now().plusSeconds(Math.min(300, 10L * (entry.getAttempts()+1))));
                            return outboxRepository.save(entry).then();
                        }))
                .subscribe();
    }
}
