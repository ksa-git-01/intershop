package ru.yandex.practicum.store.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import ru.yandex.practicum.store.model.Order;

public interface OrderRepository extends R2dbcRepository<Order, Long> {
}