package ru.yandex.practicum.intershop.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.intershop.dto.CartItemAction;
import ru.yandex.practicum.intershop.model.Cart;
import ru.yandex.practicum.intershop.model.Item;
import ru.yandex.practicum.intershop.dto.ItemSort;
import ru.yandex.practicum.intershop.repository.CartRepository;
import ru.yandex.practicum.intershop.repository.ItemRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    public Page<Item> findAll(String search, ItemSort sort, Integer pageNumber, Integer pageSize) {
        if (search == null) {
            return itemRepository.findAll(PageRequest.of(pageNumber, pageSize, itemSortToSort(sort)));
        } else {
            return itemRepository.findAllByTitleContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
                    search,
                    search,
                    PageRequest.of(pageNumber, pageSize, itemSortToSort(sort))
                    );
        }
    }
    private Sort itemSortToSort (ItemSort itemSort) {
        return switch (itemSort) {
            case ALPHA -> Sort.by("title").ascending();
            case PRICE -> Sort.by("price").ascending();
            case NO -> Sort.unsorted();
        };
    }
}
