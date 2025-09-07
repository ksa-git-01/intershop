package ru.yandex.practicum.payment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.api.PaymentApi;
import ru.yandex.practicum.payment.mapper.BalanceMapper;
import ru.yandex.practicum.payment.mapper.PaymentMapper;
import ru.yandex.practicum.payment.model.GetBalance200Response;
import ru.yandex.practicum.payment.model.PostPayment200Response;
import ru.yandex.practicum.payment.model.PostPaymentRequest;
import ru.yandex.practicum.payment.service.PaymentService;

@Controller
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {

    private final PaymentService paymentService;
    private final BalanceMapper balanceMapper;
    private final PaymentMapper paymentMapper;

    @Override
    public Mono<ResponseEntity<GetBalance200Response>> getBalance(ServerWebExchange exchange) {
        return paymentService.getBalance()
                .map(balanceMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<PostPayment200Response>> postPayment(
            Mono<PostPaymentRequest> postPaymentRequest,
            ServerWebExchange exchange) {
        return postPaymentRequest
                .flatMap(request -> paymentService.processPayment(request.getOrderId(), request.getAmount()))
                .map(paymentMapper::toResponse)
                .map(ResponseEntity::ok);
    }
}