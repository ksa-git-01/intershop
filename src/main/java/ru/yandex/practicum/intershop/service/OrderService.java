package ru.yandex.practicum.intershop.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.intershop.dto.ItemView;
import ru.yandex.practicum.intershop.dto.OrderView;
import ru.yandex.practicum.intershop.model.Item;
import ru.yandex.practicum.intershop.model.Order;
import ru.yandex.practicum.intershop.model.OrderItem;
import ru.yandex.practicum.intershop.repository.OrderRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    public List<OrderView> getOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(order ->
                    OrderView.builder()
                            .id(order.getId())
                            .items(order.getOrderItems().stream()
                                    .map(this::toItemDto)
                                    .toList())
                            .build()
                    )
                .toList();
    }

    private ItemView toItemDto(OrderItem orderItem) {
        Item item = orderItem.getItem();
        return new ItemView(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getFilename(),
                orderItem.getCount(),
                orderItem.getPrice()
        );
    }

    public OrderView getOrder(Long id) {
        Optional<Order> order = orderRepository.findById(id);
        return order.map(o -> OrderView.builder()
                .id(o.getId())
                .items(o.getOrderItems().stream()
                        .map(this::toItemDto)
                        .toList())
                .build())
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
    }
}
