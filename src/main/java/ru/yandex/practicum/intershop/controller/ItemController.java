package ru.yandex.practicum.intershop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.dto.CartItemAction;
import ru.yandex.practicum.intershop.mapper.ItemMapper;
import ru.yandex.practicum.intershop.service.CartService;
import ru.yandex.practicum.intershop.service.FileService;
import ru.yandex.practicum.intershop.service.ItemService;

@Controller
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemService itemService;
    private final CartService cartService;
    private final ItemMapper itemMapper;
    private final FileService fileService;

    @GetMapping("/items/{id}")
    public Mono<String> getItem(@PathVariable(name = "id") Long id, Model model) {
        return itemService.findItemById(id)
                .doOnNext(item -> model.addAttribute("item", itemMapper.itemToItemDto(item)))
                .thenReturn("item")
                .onErrorReturn("error"); // Если товар не найден, показываем страницу ошибки
    }

    @PostMapping("/items/{id}")
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
                .thenReturn("redirect:/items/" + id);
    }

    @GetMapping("/items/add")
    public Mono<String> showAddItemForm(Model model) {
        return Mono.just("add-item");
    }

    @PostMapping("/items/add")
    public Mono<String> addItem(ServerWebExchange exchange) {
        return exchange.getMultipartData()
                .flatMap(this::processAddItemForm)
                .onErrorResume(throwable -> {
                    // В случае ошибки валидации или другой ошибки
                    return Mono.just("redirect:/items/add?error=true");
                });
    }

    private Mono<String> processAddItemForm(MultiValueMap<String, Part> parts) {
        try {
            // Извлекаем обязательные параметры
            String title = getFormValue(parts, "title");
            String description = getFormValue(parts, "description");
            Integer count = Integer.valueOf(getFormValue(parts, "count"));
            Double price = Double.valueOf(getFormValue(parts, "price"));

            // Валидация
            if (count < 1) {
                return Mono.error(new IllegalArgumentException("Count must be at least 1"));
            }
            if (price < 1) {
                return Mono.error(new IllegalArgumentException("Price must be at least 1"));
            }

            // Получаем файл изображения (опционально)
            Mono<String> filenameMono = getImageFile(parts)
                    .flatMap(fileService::saveImage)
                    .defaultIfEmpty("");

            return filenameMono
                    .flatMap(filename -> itemService.addItem(title, description, count, price, filename))
                    .map(itemId -> "redirect:/items/" + itemId);

        } catch (Exception e) {
            return Mono.error(new IllegalArgumentException("Invalid form data: " + e.getMessage()));
        }
    }

    private String getFormValue(MultiValueMap<String, Part> parts, String key) {
        var part = parts.getFirst(key);
        if (part instanceof FormFieldPart formField) {
            return formField.value();
        }
        throw new IllegalArgumentException("Missing required field: " + key);
    }

    private Mono<FilePart> getImageFile(MultiValueMap<String, Part> parts) {
        var imagePart = parts.getFirst("image");
        if (imagePart instanceof FilePart filePart) {
            return Mono.just(filePart);
        }
        return Mono.empty(); // Файл не загружен
    }
}