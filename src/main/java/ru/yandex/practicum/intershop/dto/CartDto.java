package ru.yandex.practicum.intershop.dto;

import lombok.Builder;
import lombok.Getter;
import ru.yandex.practicum.intershop.model.Item;

import java.util.List;

@Builder
@Getter
public class CartDto {
    List<ItemDto> items;
    Double total;
    Boolean empty;
}
