package ru.yandex.practicum.store.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.store.model.Item;

public interface ItemRepository extends R2dbcRepository<Item, Long> {
    Flux<Item> findAllByTitleContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
            String searchTitleString,
            String searchDescriptionString,
            Pageable pageable
    );

    Flux<Item> findAllByTitleContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
            String searchTitleString,
            String searchDescriptionString
    );
}