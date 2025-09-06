package ru.yandex.practicum.store.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.store.dto.ItemView;
import ru.yandex.practicum.store.model.Item;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    @Mapping(source = "filename", target = "imgPath")
    ItemView itemToItemDto(Item value);
}
