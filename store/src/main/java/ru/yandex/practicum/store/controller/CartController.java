package ru.yandex.practicum.store.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.store.dto.CartItemAction;
import ru.yandex.practicum.store.service.CartService;

@Controller
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping("/cart/items")
    public Mono<String> getCart(Model model) {
        return cartService.getCart()
                .doOnNext(cart -> {
                    model.addAttribute("items", cart.getItems());
                    model.addAttribute("total", cart.getTotal());
                    model.addAttribute("empty", cart.getEmpty());
                })
                .thenReturn("cart");
    }

    @PostMapping("/cart/items/{id}")
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
                .thenReturn("redirect:/cart/items");
    }
}