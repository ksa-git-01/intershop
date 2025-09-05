package ru.yandex.practicum.payment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.dto.BalanceDto;
import ru.yandex.practicum.payment.dto.PaymentResultDto;
import ru.yandex.practicum.payment.exception.InsufficientFundsException;

@Service
public class PaymentService {
    // Для простоты читаем начальный баланс из property файла и храним в сервисе
    @Value("${payment.initial-balance}")
    private Double currentBalance;

    public Mono<BalanceDto> getBalance() {
        return Mono.just(new BalanceDto(currentBalance));
    }

    public Mono<PaymentResultDto> processPayment(Long orderId, Double amount) {
        return Mono.fromCallable(() -> {
            if (orderId == null || orderId <= 0) {
                throw new IllegalArgumentException("Некорректный ID заказа");
            }

            if (amount == null || amount <= 0) {
                throw new IllegalArgumentException("Сумма платежа должна быть положительной");
            }

            if (currentBalance < amount) {
                throw new InsufficientFundsException("Недостаточно средств для выполнения платежа. Доступно: " + currentBalance + ", требуется: " + amount);
            }

            currentBalance = currentBalance - amount;

            return new PaymentResultDto(true, currentBalance);
        });
    }
}