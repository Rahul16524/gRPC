package com.example.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderItemDto {
    private Long id;
    @JsonIgnore
    private OrderDto order;
    @NotNull(message = "Product id is required")
    @Positive(message = "Product id must be greater than zero")
    private Long productId;
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    private Integer quantity;
    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", message = "Unit price must not be negative")
    private Double unitPrice;

    private Double totalAmount;


}
