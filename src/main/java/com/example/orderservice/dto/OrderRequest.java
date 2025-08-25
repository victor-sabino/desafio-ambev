
package com.example.orderservice.dto;

import com.example.orderservice.domain.OrderItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    @NotBlank
    private String externalOrderId;
    @NotBlank
    private String customerId;
    @NotEmpty
    private List<OrderItem> items;
}
