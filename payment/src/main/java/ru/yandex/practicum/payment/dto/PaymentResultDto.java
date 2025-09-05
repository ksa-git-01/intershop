package ru.yandex.practicum.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResultDto {
    private boolean success;
    private Double remainingBalance;
}