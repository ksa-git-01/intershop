package ru.yandex.practicum.intershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.intershop.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
