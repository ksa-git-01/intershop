package ru.yandex.practicum.intershop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.intershop.dto.CartItemAction;
import ru.yandex.practicum.intershop.dto.ItemDto;
import ru.yandex.practicum.intershop.dto.Paging;
import ru.yandex.practicum.intershop.mapper.ItemMapper;
import ru.yandex.practicum.intershop.model.Item;
import ru.yandex.practicum.intershop.dto.ItemSort;
import ru.yandex.practicum.intershop.service.CartService;
import ru.yandex.practicum.intershop.service.ItemService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final ItemService itemService;
    private final CartService cartService;
    private final ItemMapper itemMapper;
    @GetMapping("/")
    public String redirectToMain() {
        return "redirect:/main/items";
    }
    @GetMapping("/main/items")
    public String showMain(@RequestParam(name = "search", required = false) String search,
                           @RequestParam(name = "sort", defaultValue = "NO" , required = false) ItemSort sort,
                           @RequestParam(name = "pageNumber", defaultValue = "0", required = false) Integer pageNumber,
                           @RequestParam(name = "pageSize", defaultValue = "10", required = false) Integer pageSize,
                           Model model) {
        Page<Item> itemsPage = itemService.findAll(search, sort, pageNumber, pageSize);
        List<Item> itemList = itemsPage.getContent();

        model.addAttribute("items", getItemsForModel(itemList));
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        model.addAttribute("paging", Paging.builder()
                .pageNumber(itemsPage.getNumber())
                .pageSize(itemsPage.getSize())
                .hasNext(itemsPage.hasNext())
                .hasPrevious(itemsPage.hasPrevious())
                .build());
        return "main";
    }
    @PostMapping("/main/items/{id}")
    public String modifyCartItem (@PathVariable(name = "id") Long id,
                                  @RequestParam(name = "action") CartItemAction action) {
        cartService.modifyCartByItem(id, action);
        return "redirect:/main/items";
    }
    private List<List<ItemDto>> getItemsForModel(List<Item> items) {
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
