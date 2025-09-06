package ru.yandex.practicum.store.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class CartView {
    List<ItemView> items;
    Double total;
    Boolean empty;
}
