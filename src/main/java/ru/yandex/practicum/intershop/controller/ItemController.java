package ru.yandex.practicum.intershop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.intershop.dto.CartItemAction;
import ru.yandex.practicum.intershop.mapper.ItemMapper;
import ru.yandex.practicum.intershop.model.Item;
import ru.yandex.practicum.intershop.service.CartService;
import ru.yandex.practicum.intershop.service.FileService;
import ru.yandex.practicum.intershop.service.ItemService;
import jakarta.validation.constraints.Min;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemService itemService;
    private final CartService cartService;
    private final ItemMapper itemMapper;
    private final FileService fileService;

    @GetMapping("/items/{id}")
    public String getItem(@PathVariable(name = "id") Long id,
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

    @GetMapping("/items/add")
    public String showAddItemForm(Model model) {
        return "add-item";
    }

    @PostMapping("/items/add")
    public String addItem(@RequestParam("title") String title,
                        @RequestParam("description") String description,
                        @RequestParam("count") @Min(value = 1) Integer count,
                        @RequestParam("price") @Min(value = 1) Double price,
                        @RequestParam(value = "image", required = false) MultipartFile image) {
        String filename = fileService.saveImage(image);
        Long itemId = itemService.addItem(title, description, count, price, filename);
        return "redirect:/items/" + itemId;
    }
}
