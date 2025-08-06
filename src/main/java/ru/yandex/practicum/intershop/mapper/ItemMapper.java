package ru.yandex.practicum.intershop.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.intershop.dto.ItemMainDto;
import ru.yandex.practicum.intershop.model.Item;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    @Mapping(source = "filename", target = "imgPath")
    ItemMainDto itemToItemMainDto (Item value);
}
