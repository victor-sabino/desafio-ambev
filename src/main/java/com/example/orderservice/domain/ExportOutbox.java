
package com.example.orderservice.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "outbox_export")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportOutbox {
    @Id private String id;
    private String orderId;
    private Instant nextAttemptAt;
    private int attempts;
    private String lastError;
}
