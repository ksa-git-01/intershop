package ru.yandex.practicum.intershop.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.intershop.configuration.BasicTestConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderControllerTest extends BasicTestConfiguration {
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
}