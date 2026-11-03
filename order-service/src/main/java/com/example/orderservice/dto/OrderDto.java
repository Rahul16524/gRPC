package com.example.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {

    private Long id;

    @NotNull(message = "Customer id is required")
    @Positive(message = "Customer id must be greater than zero")
    private Long customerId;

    private LocalDateTime orderDate;

    private Double totalPrice;

    @NotEmpty(message = "An order must contain at least one item")
    @Valid
    private List<OrderItemDto> items;

    private String transactionId;


}
