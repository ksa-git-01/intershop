package ru.yandex.practicum.store.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.store.service.OrderService;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/orders")
    public Mono<String> getOrders(Model model) {
        return orderService.getOrders()
                .collectList()
                .doOnNext(orders -> model.addAttribute("orders", orders))
                .thenReturn("orders");
    }

    @GetMapping("/orders/{id}")
    public Mono<String> getOrder(@PathVariable(name = "id") Long id,
                                 @RequestParam(name = "newOrder", defaultValue = "false", required = false) Boolean newOrder,
                                 Model model) {
        return orderService.getOrder(id)
                .doOnNext(order -> {
                    model.addAttribute("order", order);
                    model.addAttribute("newOrder", newOrder);
                })
                .thenReturn("order");
    }

    @PostMapping("/buy")
    public Mono<String> buy() {
        return orderService.buy()
                .map(orderId -> "redirect:/orders/" + orderId + "?newOrder=true");
    }
}