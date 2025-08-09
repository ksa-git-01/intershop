package ru.yandex.practicum.intershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.intershop.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
