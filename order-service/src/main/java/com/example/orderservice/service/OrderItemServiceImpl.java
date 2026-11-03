package com.example.orderservice.service;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.dto.OrderItemDto;
import com.example.orderservice.mapper.OrderItemMapper;
import com.example.orderservice.repository.OrderItemRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@AllArgsConstructor
@Service
public class OrderItemServiceImpl implements OrderItemService {
    private final OrderItemRepository orderItemRepository;
    private final OrderItemMapper orderItemMapper;
    private final OrderService orderService;

    @Override
    public OrderItemDto findById(Long id) {
        return orderItemMapper.toDto(orderItemRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found: " + id)));
    }

    @Override
    public OrderItemDto create(OrderItemDto orderItemDto) {
        orderItemDto.setTotalAmount(calculateTotalAmount(orderItemDto));
        return orderItemMapper.toDto(orderItemRepository.save(orderItemMapper.toEntity(orderItemDto)));
    }

    @Override
    public List<OrderItemDto> create(List<OrderItemDto> orderItemDtos) {
        orderItemDtos.forEach(item -> item.setTotalAmount(calculateTotalAmount(item)));
        return orderItemMapper.toDto(orderItemRepository.saveAll(orderItemMapper.toEntity(orderItemDtos)));
    }

    @Override
    public OrderItemDto create(OrderItemDto orderItemDto, Long orderId) {
        orderItemDto.setOrder(orderService.findById(orderId));
        orderItemDto.setTotalAmount(calculateTotalAmount(orderItemDto));
        return orderItemMapper.toDto(orderItemRepository.save(orderItemMapper.toEntity(orderItemDto)));
    }

    @Override
    public List<OrderItemDto> create(List<OrderItemDto> orderItemDtos, Long orderId) {
        OrderDto orderDto = orderService.findById(orderId);
        orderItemDtos.forEach(item -> {
            item.setOrder(orderDto);
            item.setTotalAmount(calculateTotalAmount(item));
        });
        return orderItemMapper.toDto(orderItemRepository.saveAll(orderItemMapper.toEntity(orderItemDtos)));
    }

    private double calculateTotalAmount(OrderItemDto item) {
        double totalAmount = item.getQuantity() * item.getUnitPrice();
        if (!Double.isFinite(totalAmount)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order-item total is too large");
        }
        return totalAmount;
    }
}
