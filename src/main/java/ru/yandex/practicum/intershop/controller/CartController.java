package ru.yandex.practicum.intershop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.intershop.dto.CartView;
import ru.yandex.practicum.intershop.dto.CartItemAction;
import ru.yandex.practicum.intershop.service.CartService;

@Controller
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping("/cart/items")
    public String getCart(Model model){
        CartView cart = cartService.getCart();
        model.addAttribute("items", cart.getItems());
        model.addAttribute("total", cart.getTotal());
        model.addAttribute("empty", cart.getEmpty());
        return "cart";
    }

    @PostMapping("/cart/items/{id}")
    public String modifyCartItem (@PathVariable(name = "id") Long id,
                                  @RequestParam(name = "action") CartItemAction action) {
        cartService.modifyCartByItem(id, action);
        return "redirect:/cart/items";
    }
}
