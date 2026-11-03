package com.example.orderservice.service;

import com.example.orderservice.client.PaymentClient;
import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.dto.OrderItemDto;
import com.example.orderservice.mapper.OrderMapper;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.repository.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final PaymentClient paymentClient;

    @Override
    public OrderDto findById(Long id) {
        return orderMapper.toDto(orderRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + id)));
    }

    @Override
    @Transactional
    public OrderDto createOrder(OrderDto orderDto) {
        if (orderDto.getItems() == null || orderDto.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "An order must contain at least one item");
        }

        Order order = new Order();
        order.setCustomerId(orderDto.getCustomerId());
        order.setOrderDate(LocalDateTime.now());

        List<OrderItem> items = new ArrayList<>();
        double totalPrice = 0;
        for (OrderItemDto itemDto : orderDto.getItems()) {
            if (itemDto.getProductId() == null || itemDto.getQuantity() == null || itemDto.getQuantity() <= 0
                    || itemDto.getUnitPrice() == null || !Double.isFinite(itemDto.getUnitPrice())
                    || itemDto.getUnitPrice() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Each item must have a productId, positive quantity, and non-negative unitPrice");
            }

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(itemDto.getProductId());
            item.setQuantity(itemDto.getQuantity());
            item.setUnitPrice(itemDto.getUnitPrice());
            double itemTotal = itemDto.getQuantity() * itemDto.getUnitPrice();
            if (!Double.isFinite(itemTotal) || !Double.isFinite(totalPrice + itemTotal)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order total is too large");
            }
            item.setTotalAmount(itemTotal);
            items.add(item);
            totalPrice += itemTotal;
        }

        order.setItems(items);
        order.setTotalPrice(totalPrice);
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderDto processPayment(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + orderId));
        OrderDto orderDto = orderMapper.toDto(order);
        orderDto.setTotalPrice(calculateTotalAmount(orderDto));
        String transactionId = paymentClient.processPayment(orderDto.getId().toString(),
                orderDto.getTotalPrice(), "USD");
        orderDto.setTransactionId(transactionId);
        order.setTotalPrice(orderDto.getTotalPrice());
        order.setTransactionId(transactionId);
        orderRepository.save(order);
        return orderDto;
    }

    private Double calculateTotalAmount(OrderDto orderDto) {
        return orderDto.getItems().stream()
                .map(OrderItemDto::getTotalAmount)
                .reduce(0.0, Double::sum);
    }
}
