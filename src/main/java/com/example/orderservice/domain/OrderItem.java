package com.example.orderservice.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    @NotBlank
    private String productId;
    @NotBlank
    private String name;
    @NotNull
    @Min(0)
    private BigDecimal price;
    @Min(1)
    private int quantity;
}
