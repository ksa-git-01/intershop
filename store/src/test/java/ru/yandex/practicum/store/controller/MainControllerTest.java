package ru.yandex.practicum.store.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.yandex.practicum.store.configuration.BasicTestConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

class MainControllerTest extends BasicTestConfiguration {

    @BeforeEach
    void setUpData() {
        insertItem("Товар 1", "Описание товара 1", "image1.jpg", 100, 999.99);
        insertItem("Товар 2", "Описание товара 2", "image2.jpg", 200, 199.50);
    }

    @Test
    void rootRedirectsToMain() {
        webTestClient.get()
                .uri("/")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/main/items");
    }

    @Test
    void getMainItems() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/main/items")
                        .queryParam("search", "")
                        .queryParam("sort", "NO")
                        .queryParam("pageNumber", "0")
                        .queryParam("pageSize", "10")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertThat(body).contains("Витрина товаров");
                    assertThat(body).contains("Товар 1");
                });
    }

    @Test
    void modifyCartItemPlusItem() {
        long itemId = insertItem("Товар 3", "Описание товара 3", "image3.jpg", 5, 2999.00);
        insertCart(itemId, 1);

        Integer before = getCartCount(itemId).block();
        assertThat(before).isEqualTo(1);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("action", "PLUS");

        webTestClient.post()
                .uri("/main/items/{id}", itemId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/main/items");

        Integer after = getCartCount(itemId).block();
        assertThat(after).isEqualTo(2);
    }

    @Test
    void modifyCartItemMinusItem() {
        long itemId = insertItem("Товар 3", "Описание товара 3", "image3.jpg", 5, 2999.00);
        insertCart(itemId, 2);

        Integer before = getCartCount(itemId).block();
        assertThat(before).isEqualTo(2);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("action", "MINUS");

        webTestClient.post()
                .uri("/main/items/{id}", itemId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/main/items");

        Integer after = getCartCount(itemId).block();
        assertThat(after).isEqualTo(1);
    }

    @Test
    void modifyCartItemDeleteItem() {
        long itemId = insertItem("Товар 3", "Описание товара 3", "image3.jpg", 5, 2999.00);
        insertCart(itemId, 2);

        Integer before = getCartCount(itemId).block();
        assertThat(before).isEqualTo(2);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("action", "DELETE");

        webTestClient.post()
                .uri("/main/items/{id}", itemId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/main/items");

        Integer after = getCartItemsCount(itemId).block();
        assertThat(after).isEqualTo(0);
    }
}