package ru.yandex.practicum.intershop.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ItemMainDto {
    private Long id;
    private String title;
    private String description;
    private String imgPath;
    private Integer count;
    private Double price;
}
