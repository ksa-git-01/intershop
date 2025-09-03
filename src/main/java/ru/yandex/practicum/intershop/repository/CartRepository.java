package ru.yandex.practicum.intershop.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.model.Cart;

public interface CartRepository extends R2dbcRepository<Cart, Long> {
    Mono<Cart> findByItemId(Long itemId);
}