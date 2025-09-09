package ru.yandex.practicum.payment.mapper;

import org.mapstruct.Mapper;
import ru.yandex.practicum.payment.dto.PaymentResultDto;
import ru.yandex.practicum.payment.model.PostPayment200Response;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PostPayment200Response toResponse(PaymentResultDto dto);
}