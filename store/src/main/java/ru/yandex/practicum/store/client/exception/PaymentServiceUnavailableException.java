package ru.yandex.practicum.store.client.exception;

public class PaymentServiceUnavailableException extends RuntimeException {
    public PaymentServiceUnavailableException(String message) {
        super(message);
    }
}
