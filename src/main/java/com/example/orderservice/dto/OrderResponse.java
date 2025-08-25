
package com.example.orderservice.dto;

import com.example.orderservice.domain.OrderItem;
import com.example.orderservice.domain.OrderStatus;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String id;
    private String externalOrderId;
    private String customerId;
    private List<OrderItem> items;
    private BigDecimal total;
    private OrderStatus status;
}
