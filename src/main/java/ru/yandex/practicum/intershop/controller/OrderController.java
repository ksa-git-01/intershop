package ru.yandex.practicum.intershop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.intershop.dto.OrderView;
import ru.yandex.practicum.intershop.service.OrderService;
import org.springframework.ui.Model;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/orders")
    public String getOrders(Model model) {
        List<OrderView> orders = orderService.getOrders();
        model.addAttribute("orders", orders);
        return "orders";
    }

    @GetMapping("/orders/{id}")
    public String getOrder(@PathVariable(name = "id") Long id,
                           @RequestParam(name = "newOrder", defaultValue = "false", required = false) Boolean newOrder,
                           Model model) {
        OrderView order = orderService.getOrder(id);
        model.addAttribute("order", order);
        model.addAttribute("newOrder", newOrder);
        return "order";
    }
}
