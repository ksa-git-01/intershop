package ru.yandex.practicum.payment.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.payment.dto.BalanceDto;
import ru.yandex.practicum.payment.model.GetBalance200Response;

@Mapper(componentModel = "spring")
public interface BalanceMapper {
    GetBalance200Response toResponse(BalanceDto dto);
}