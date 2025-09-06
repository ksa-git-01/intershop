package ru.yandex.practicum.store.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Paging {
    Integer pageNumber;
    Integer pageSize;
    Boolean hasNext;
    Boolean hasPrevious;
}
