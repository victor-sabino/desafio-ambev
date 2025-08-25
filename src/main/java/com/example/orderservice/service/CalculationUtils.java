
package com.example.orderservice.service;

import com.example.orderservice.domain.OrderItem;
import java.math.BigDecimal;
import java.util.List;

public class CalculationUtils {
    public static BigDecimal totalOf(List<OrderItem> items) {
        return items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
