package ru.yandex.practicum.payment.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestPropertySource(properties = "payment.initial-balance=10000")
class PaymentControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void getBalance() {
        webTestClient.get()
                .uri("/balance")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.balance").isEqualTo(10000.0);
    }

    @Test
    void postPaymentWithValidData() {
        String requestBody = """
                {
                    "amount": 1000.0
                }
                """;

        webTestClient.post()
                .uri("/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.remainingBalance").isEqualTo(9000.0);
    }

    @Test
    void postPaymentWithInsufficientFunds() {
        String requestBody = """
                {
                    "amount": 15000.0
                }
                """;

        webTestClient.post()
                .uri("/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo("Insufficient funds")
                .jsonPath("$.message").isEqualTo("Недостаточно средств для выполнения платежа. Доступно: 10000.0, требуется: 15000.0");
    }

    @Test
    void postPaymentWithNullAmount() {
        String requestBody = """
                {
                    "amount": null
                }
                """;

        webTestClient.post()
                .uri("/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo("Invalid request")
                .jsonPath("$.message").isEqualTo("Некорректный формат данных");
    }


    @Test
    void postPaymentWithZeroAmount() {
        String requestBody = """
                {
                    "amount": 0
                }
                """;

        webTestClient.post()
                .uri("/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo("Invalid request")
                .jsonPath("$.message").isEqualTo("Сумма платежа должна быть положительной");
    }
}