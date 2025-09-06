package ru.yandex.practicum.payment.exception;

import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.model.Error;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InsufficientFundsException.class)
    public Mono<ResponseEntity<Error>> handleInsufficientFunds(InsufficientFundsException ex) {
        Error error = new Error("Insufficient funds", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<Error>> handleIllegalArgument(IllegalArgumentException ex) {
        Error error = new Error("Invalid request", ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<Error>> handleServerWebInput(ServerWebInputException ex) {
        Error error = new Error("Invalid request", "Некорректный формат данных");
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }

    @ExceptionHandler(DecodingException.class)
    public Mono<ResponseEntity<Error>> handleDecoding(DecodingException ex) {
        Error error = new Error("Invalid request", "Ошибка декодирования данных");
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Error>> handleGeneral(Exception ex) {
        Error error = new Error("Internal Server Error", "Произошла внутренняя ошибка сервера");
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
    }
}