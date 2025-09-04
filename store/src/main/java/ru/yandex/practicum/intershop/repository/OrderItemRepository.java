package ru.yandex.practicum.intershop.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.intershop.model.OrderItem;

public interface OrderItemRepository extends R2dbcRepository<OrderItem, Long> {
    Flux<OrderItem> findByOrderId(Long orderId);
}