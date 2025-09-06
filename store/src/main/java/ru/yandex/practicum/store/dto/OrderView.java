package ru.yandex.practicum.store.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Builder
@Getter
public class OrderView {
    Long id;
    List<ItemView> items;

    public double totalSum() {
        return BigDecimal.valueOf(items.stream().mapToDouble(i -> i.getPrice() * i.getCount()).sum())
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
