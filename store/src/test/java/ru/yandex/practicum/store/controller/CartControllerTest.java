package ru.yandex.practicum.store.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.yandex.practicum.store.client.PaymentClient;
import ru.yandex.practicum.store.client.exception.PaymentServiceUnavailableException;
import ru.yandex.practicum.store.configuration.BasicTestConfiguration;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.store.client.model.GetBalance200Response;

import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.assertThat;

public class CartControllerTest extends BasicTestConfiguration {

    @MockitoBean
    private PaymentClient paymentClient;

    @BeforeEach
    void setUpData() {
        insertItem("Товар 1", "Описание товара 1", "image1.jpg", 100, 999.99);
        insertItem("Товар 2", "Описание товара 2", "image2.jpg", 200, 199.50);
        insertCart(1L, 2);
        insertCart(2L, 3);
    }

    @Test
    void getCartModel() {
        when(paymentClient.getBalance())
                .thenReturn(Mono.just(new GetBalance200Response().balance(1500.0)));

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertThat(body).contains("Корзина товаров");
                    assertThat(body).contains("Товар 1");
                    assertThat(body).contains("Баланс: 1500.0 руб.");
                });
    }

    @Test
    void modifyCartItemPlusItem() {
        Integer before = getCartCount(1L).block();
        assertThat(before).isEqualTo(2);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("action", "PLUS");

        webTestClient.post()
                .uri("/cart/items/{id}", 1L)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/cart/items");

        Integer after = getCartCount(1L).block();
        assertThat(after).isEqualTo(3);
    }

    @Test
    void modifyCartItemMinus() {
        Integer before = getCartCount(2L).block();
        assertThat(before).isEqualTo(3);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("action", "MINUS");

        webTestClient.post()
                .uri("/cart/items/{id}", 2L)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/cart/items");

        Integer after = getCartCount(2L).block();
        assertThat(after).isEqualTo(2);
    }

    @Test
    void modifyCartItemDelete() {
        Integer before = getCartCount(2L).block();
        assertThat(before).isEqualTo(3);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("action", "DELETE");

        webTestClient.post()
                .uri("/cart/items/{id}", 2L)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/cart/items");

        Integer after = getCartItemsCount(2L).block();
        assertThat(after).isEqualTo(0);
    }

    @Test
    void getCartModelWhenPaymentServiceUnavailable() {
        when(paymentClient.getBalance())
                .thenReturn(Mono.error(new PaymentServiceUnavailableException("Сервис оплаты недоступен")));

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertThat(body).contains("Корзина товаров");
                    assertThat(body).contains("Товар 1");
                    assertThat(body).contains("Сервис оплаты недоступен");
                });
    }

    @Test
    void getCartModelWhenInsufficientFunds() {
        when(paymentClient.getBalance())
                .thenReturn(Mono.just(new GetBalance200Response().balance(50.0)));

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertThat(body).contains("Корзина товаров");
                    assertThat(body).contains("Товар 1");
                    assertThat(body).contains("Недостаточно средств");
                });
    }
}