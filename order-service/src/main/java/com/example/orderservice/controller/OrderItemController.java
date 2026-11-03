package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderItemDto;
import com.example.orderservice.service.OrderItemService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/order-item")
public class OrderItemController {
    private final OrderItemService orderItemService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderItemDto createStandaloneOrderItem(@RequestBody @Valid OrderItemDto orderItemDto) {
        return orderItemService.create(orderItemDto);
    }

    @PostMapping("/list")
    @ResponseStatus(HttpStatus.CREATED)
    public List<OrderItemDto> createStandaloneOrderItems(
            @RequestBody @NotEmpty List<@NotNull @Valid OrderItemDto> orderItemDtos) {
        return orderItemService.create(orderItemDtos);
    }

    @PostMapping("/create/order/{orderId}")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderItemDto createOrderItem(
            @RequestBody @Valid OrderItemDto orderItemDto, @PathVariable Long orderId) {
        return orderItemService.create(orderItemDto, orderId);
    }

    @PostMapping("/create-list/order/{orderId}")
    @ResponseStatus(HttpStatus.CREATED)
    public List<OrderItemDto> createOrderItems(
            @RequestBody @NotEmpty List<@NotNull @Valid OrderItemDto> orderItemDtos, @PathVariable Long orderId) {
        return orderItemService.create(orderItemDtos, orderId);
    }
}
