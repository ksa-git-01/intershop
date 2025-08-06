package ru.yandex.practicum.intershop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.intershop.model.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Page<Item> findAllByTitleContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
            String searchTitleString,
            String searchDescriptionString,
            Pageable pageable
    );
}
