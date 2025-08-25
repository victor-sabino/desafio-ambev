
package com.example.orderservice.service;

import com.example.orderservice.domain.OrderItem;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculationUtilsTest {
    @Test
    void shouldSumTotalsOfItems() {
        var items = List.of(
                OrderItem.builder().productId("P1").name("A").price(new BigDecimal("10.50")).quantity(2).build(),
                OrderItem.builder().productId("P2").name("B").price(new BigDecimal("5")).quantity(1).build()
        );
        var total = CalculationUtils.totalOf(items);
        assertEquals(new BigDecimal("26.00"), total);
    }
}
