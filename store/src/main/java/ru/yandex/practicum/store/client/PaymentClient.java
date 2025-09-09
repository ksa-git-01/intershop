package ru.yandex.practicum.store.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.store.client.api.PaymentApi;
import ru.yandex.practicum.store.client.exception.InsufficientFundsException;
import ru.yandex.practicum.store.client.exception.PaymentServiceUnavailableException;
import ru.yandex.practicum.store.client.model.GetBalance200Response;
import ru.yandex.practicum.store.client.model.PostPayment200Response;
import ru.yandex.practicum.store.client.model.PostPaymentRequest;

@Component
public class PaymentClient {
    private final PaymentApi paymentApi;

    public PaymentClient() {
        this.paymentApi = new PaymentApi();
    }

    public Mono<GetBalance200Response> getBalance() {
        return paymentApi.getBalance()
                .onErrorMap(WebClientRequestException.class, ex -> new PaymentServiceUnavailableException("Сервис оплаты недоступен"));
    }

    public Mono<PostPayment200Response> postPayment(PostPaymentRequest postPaymentRequest) {
        return paymentApi.postPayment(postPaymentRequest)
                .onErrorMap(WebClientRequestException.class, ex -> new PaymentServiceUnavailableException("Сервис оплаты недоступен"))
                .onErrorMap(WebClientResponseException.BadRequest.class, ex -> new InsufficientFundsException("Недостаточно средств для выполнения платежа"))
                .onErrorMap(WebClientResponseException.InternalServerError.class, ex -> new PaymentServiceUnavailableException("Внутренняя ошибка сервиса платежей"));

    }
}
