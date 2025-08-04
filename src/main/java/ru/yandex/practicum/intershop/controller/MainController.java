package ru.yandex.practicum.intershop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    @GetMapping("/")
    public String redirectToMain() {
        return "redirect:/main/items";
    }
    @GetMapping("/main/items")
    public String showMain(Model model) {
        return "main";
    }
}
