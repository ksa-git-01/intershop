package ru.yandex.practicum.store.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.store.dto.CartItemAction;
import ru.yandex.practicum.store.dto.ItemSort;
import ru.yandex.practicum.store.dto.ItemView;
import ru.yandex.practicum.store.dto.Paging;
import ru.yandex.practicum.store.mapper.ItemMapper;
import ru.yandex.practicum.store.model.Item;
import ru.yandex.practicum.store.service.CartService;
import ru.yandex.practicum.store.service.ItemService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final ItemService itemService;
    private final CartService cartService;
    private final ItemMapper itemMapper;

    @GetMapping("/")
    public Mono<String> redirectToMain() {
        return Mono.just("redirect:/main/items");
    }

    @GetMapping("/main/items")
    public Mono<String> showMain(@RequestParam(name = "search", required = false) String search,
                                 @RequestParam(name = "sort", defaultValue = "NO", required = false) ItemSort sort,
                                 @RequestParam(name = "pageNumber", defaultValue = "0", required = false) Integer pageNumberParam,
                                 @RequestParam(name = "pageSize", defaultValue = "10", required = false) Integer pageSizeParam,
                                 Model model) {

        Integer pageNumber = validatePageNumber(pageNumberParam);
        Integer pageSize = validatePageSize(pageSizeParam);

        return itemService.findAll(search, sort, pageNumber, pageSize)
                .doOnNext(itemsPage -> populateModel(model, itemsPage, search, sort))
                .thenReturn("main");
    }

    @PostMapping("/main/items/{id}")
    public Mono<String> modifyCartItem(@PathVariable(name = "id") Long id,
                                       ServerWebExchange exchange) {
        return exchange.getFormData()
                .flatMap(formData -> {
                    String actionValue = formData.getFirst("action");
                    if (actionValue == null) {
                        return Mono.error(new IllegalArgumentException("Action parameter is required"));
                    }
                    CartItemAction action = CartItemAction.valueOf(actionValue);
                    return cartService.modifyCartByItem(id, action);
                })
                .thenReturn("redirect:/main/items");
    }

    private Integer validatePageNumber(Integer pageNumber) {
        return pageNumber < 0 ? 0 : pageNumber;
    }

    private Integer validatePageSize(Integer pageSize) {
        return Set.of(5, 10, 20, 50, 100).contains(pageSize) ? pageSize : 10;
    }

    private void populateModel(Model model, Page<Item> itemsPage, String search, ItemSort sort) {
        List<Item> itemList = itemsPage.getContent();

        model.addAttribute("items", getItemsForModel(itemList));
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        model.addAttribute("paging", createPaging(itemsPage));
    }

    private Paging createPaging(Page<Item> itemsPage) {
        return Paging.builder()
                .pageNumber(itemsPage.getNumber())
                .pageSize(itemsPage.getSize())
                .hasNext(itemsPage.hasNext())
                .hasPrevious(itemsPage.hasPrevious())
                .build();
    }

    private List<List<ItemView>> getItemsForModel(List<Item> items) {
        return new ArrayList<>(IntStream.range(0, items.size())
                .boxed()
                .collect(Collectors.groupingBy(
                        index -> index / 3,
                        Collectors.mapping(
                                index -> itemMapper.itemToItemDto(items.get(index)),
                                Collectors.toList())
                ))
                .values());
    }
}