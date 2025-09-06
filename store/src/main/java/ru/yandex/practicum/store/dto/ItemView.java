package ru.yandex.practicum.store.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemView {
    private Long id;
    private String title;
    private String description;
    private String imgPath;
    private Integer count;
    private Double price;
}
