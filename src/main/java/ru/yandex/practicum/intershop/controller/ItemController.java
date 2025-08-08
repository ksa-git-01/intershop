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
import ru.yandex.practicum.intershop.dto.ItemSort;
import ru.yandex.practicum.intershop.dto.Paging;
import ru.yandex.practicum.intershop.mapper.ItemMapper;
import ru.yandex.practicum.intershop.model.Item;
import ru.yandex.practicum.intershop.service.CartService;
import ru.yandex.practicum.intershop.service.ItemService;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final CartService cartService;
    private final ItemMapper itemMapper;

    @GetMapping("/items/{id}")
    public String showMain(@PathVariable(name = "id") Long id,
                           Model model) {
        Optional<Item> item = itemService.findItemById(id);
        item.ifPresent(value -> model.addAttribute("item", itemMapper.itemToItemDto(value)));
        return "item";
    }

    @PostMapping("/items/{id}")
    public String modifyCartItem (@PathVariable(name = "id") Long id,
                                  @RequestParam(name = "action") CartItemAction action) {
        cartService.modifyCartByItem(id, action);
        return "redirect:/items/{id}";
    }
}
