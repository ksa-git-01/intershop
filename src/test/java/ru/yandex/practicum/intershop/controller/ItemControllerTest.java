package ru.yandex.practicum.intershop.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.configuration.BasicTestConfiguration;
import ru.yandex.practicum.intershop.service.FileService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class ItemControllerTest extends BasicTestConfiguration {
    @MockitoBean
    private FileService fileService;

    private Long itemId;

    @BeforeEach
    void setUpData() {
        itemId = insertItem("Товар 1", "Описание товара 1", "image1.jpg", 100, 999.99);
    }

    @Test
    void getItem() {
        webTestClient.get()
                .uri("/items/{id}", itemId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertThat(body).contains("Товар 1");
                    assertThat(body).contains("Описание товара 1");
                });
    }

    @Test
    void modifyCartItemPlus() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("action", "PLUS");

        webTestClient.post()
                .uri("/items/{id}", itemId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/items/" + itemId);

        Integer inCart = getCartCount(itemId).block();
        assertThat(inCart).isEqualTo(1);
    }

    @Test
    void showAddItemForm() {
        webTestClient.get()
                .uri("/items/add")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertThat(body).contains("Добавление нового товара");
                    assertThat(body).contains("action=\"/items/add\"");
                });
    }

    @Test
    void addItem() {
        reset(fileService);
        when(fileService.saveImage(any(FilePart.class))).thenReturn(Mono.just("filename.jpg"));

        Integer before = getTotalCount("item").block();
        assertThat(before).isEqualTo(1);

        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("title", "Новый товар");
        formData.add("description", "Описание");
        formData.add("count", "5");
        formData.add("price", "123.45");

        webTestClient.post()
                .uri("/items/add")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(formData))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", "/items/\\d+");

        Integer after = getTotalCount("item").block();
        assertThat(after).isEqualTo(2);
    }
}