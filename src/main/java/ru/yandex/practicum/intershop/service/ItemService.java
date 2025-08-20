package ru.yandex.practicum.intershop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.intershop.dto.ItemSort;
import ru.yandex.practicum.intershop.model.Item;
import ru.yandex.practicum.intershop.repository.ItemRepository;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    public Mono<Item> findItemById(Long id) {
        return itemRepository.findById(id);
    }

    public Mono<Page<Item>> findAll(String search, ItemSort sort, Integer pageNumber, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, itemSortToSort(sort));

        if (search == null) {
            return findAllItems(pageRequest);
        } else {
            return searchItems(search, pageRequest);
        }
    }

    private Mono<Page<Item>> findAllItems(PageRequest pageRequest) {
        return getPagedResults(
                itemRepository.findAll().skip(pageRequest.getOffset()).take(pageRequest.getPageSize()),
                itemRepository.count(),
                pageRequest
        );
    }

    private Mono<Page<Item>> searchItems(String search, PageRequest pageRequest) {
        Flux<Item> searchResults = itemRepository.findAllByTitleContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
                search, search, pageRequest);

        Mono<Long> totalCount = itemRepository.findAllByTitleContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
                search, search).count();

        return getPagedResults(searchResults, totalCount, pageRequest);
    }

    private Mono<Page<Item>> getPagedResults(Flux<Item> pagedItems, Mono<Long> totalCount, PageRequest pageRequest) {
        return pagedItems
                .collectList()
                .zipWith(totalCount)
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageRequest, tuple.getT2()));
    }

    private Sort itemSortToSort(ItemSort itemSort) {
        return switch (itemSort) {
            case ALPHA -> Sort.by("title").ascending();
            case PRICE -> Sort.by("price").ascending();
            case NO -> Sort.unsorted();
        };
    }

    public Mono<Long> addItem(String title, String description, Integer count, Double price, String filename) {
        Item item = new Item();
        item.setTitle(title);
        item.setDescription(description);
        item.setCount(count);
        item.setPrice(price);
        item.setFilename(filename);

        return itemRepository.save(item)
                .map(Item::getId);
    }
}