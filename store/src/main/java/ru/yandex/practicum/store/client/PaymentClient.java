package ru.yandex.practicum.store.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.store.client.api.PaymentApi;
import ru.yandex.practicum.store.client.exception.PaymentServiceUnavailableException;
import ru.yandex.practicum.store.client.model.GetBalance200Response;

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
}
