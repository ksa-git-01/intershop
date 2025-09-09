package ru.yandex.practicum.store.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.store.client.PaymentClient;
import ru.yandex.practicum.store.client.exception.InsufficientFundsException;
import ru.yandex.practicum.store.client.exception.PaymentServiceUnavailableException;
import ru.yandex.practicum.store.client.model.PostPayment200Response;
import ru.yandex.practicum.store.client.model.PostPaymentRequest;
import ru.yandex.practicum.store.configuration.BasicTestConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class OrderControllerTest extends BasicTestConfiguration {
    @MockitoBean
    private PaymentClient paymentClient;

    private long firstItemId;
    private long secondItemId;

    @BeforeEach
    void setUpData() {
        firstItemId = insertItem("Товар 1", "Описание товара 1", "image1.jpg", 100, 999.99);
        secondItemId = insertItem("Товар 2", "Описание товара 2", "image2.jpg", 200, 199.50);
    }

    @Test
    void getOrders() {
        long orderId = insertOrder();
        insertOrderItem(orderId, firstItemId, 2, 1999.98);

        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertThat(body).contains("Заказы");
                });
    }

    @Test
    void getOrderById() {
        long orderId = insertOrder();
        insertOrderItem(orderId, firstItemId, 1, 1999.98);

        webTestClient.get()
                .uri("/orders/{id}", orderId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertThat(body).contains("Заказ №" + orderId);
                });
    }

    @Test
    void buy() {
        when(paymentClient.postPayment(any(PostPaymentRequest.class)))
                .thenReturn(Mono.just(new PostPayment200Response()
                        .success(true)
                        .remainingBalance(500.0)));

        insertCart(firstItemId, 2);
        insertCart(secondItemId, 3);

        Integer stockFirstBefore = getItemStock(firstItemId).block();
        Integer stockSecondBefore = getItemStock(secondItemId).block();

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", "/orders/\\d+\\?newOrder=true");

        Integer ordersCount = getTotalCount("orders").block();
        Integer orderItemsCount = getTotalCount("order_items").block();
        Integer cartAfter = getTotalCount("cart").block();
        Integer stockFirstAfter = getItemStock(firstItemId).block();
        Integer stockSecondAfter = getItemStock(secondItemId).block();

        assertThat(ordersCount).isEqualTo(1);
        assertThat(orderItemsCount).isEqualTo(2);
        assertThat(cartAfter).isZero();
        assertThat(stockFirstAfter).isEqualTo(stockFirstBefore - 2);
        assertThat(stockSecondAfter).isEqualTo(stockSecondBefore - 3);
    }

    @Test
    void buyWhenPaymentServiceUnavailable() {
        insertCart(firstItemId, 1);

        when(paymentClient.postPayment(any(PostPaymentRequest.class)))
                .thenReturn(Mono.error(new PaymentServiceUnavailableException("Сервис оплаты недоступен")));

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/cart/items");

        // Проверяем что заказ не создался
        Integer ordersCount = getTotalCount("orders").block();
        assertThat(ordersCount).isZero();
    }

    @Test
    void buyWhenInsufficientFunds() {
        insertCart(firstItemId, 1);

        when(paymentClient.postPayment(any(PostPaymentRequest.class)))
                .thenReturn(Mono.error(new InsufficientFundsException("Недостаточно средств")));

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/cart/items");

        // Проверяем что заказ не создался
        Integer ordersCount = getTotalCount("orders").block();
        assertThat(ordersCount).isZero();
    }
}