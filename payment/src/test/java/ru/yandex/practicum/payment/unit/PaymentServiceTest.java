package ru.yandex.practicum.payment.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.payment.dto.BalanceDto;
import ru.yandex.practicum.payment.dto.PaymentResultDto;
import ru.yandex.practicum.payment.exception.InsufficientFundsException;
import ru.yandex.practicum.payment.service.PaymentService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = "payment.initial-balance=5000")
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    private static final Double INITIAL_BALANCE = 5000.0;

    @BeforeEach
    void setUp() {
        // Сбрасываем баланс перед каждым тестом
        ReflectionTestUtils.setField(paymentService, "currentBalance", INITIAL_BALANCE);
    }

    @Test
    void getBalance() {
        Mono<BalanceDto> result = paymentService.getBalance();

        StepVerifier.create(result)
                .assertNext(balanceDto -> {
                    assertThat(balanceDto).isNotNull();
                    assertThat(balanceDto.getBalance()).isEqualTo(INITIAL_BALANCE);
                })
                .verifyComplete();
    }

    @Test
    void processPaymentWithValidData() {
        Double amount = 1000.0;
        Double expectedRemainingBalance = INITIAL_BALANCE - amount;

        Mono<PaymentResultDto> result = paymentService.processPayment(amount);

        StepVerifier.create(result)
                .assertNext(paymentResult -> {
                    assertThat(paymentResult).isNotNull();
                    assertThat(paymentResult.isSuccess()).isTrue();
                    assertThat(paymentResult.getRemainingBalance()).isEqualTo(expectedRemainingBalance);
                })
                .verifyComplete();
    }

    @Test
    void processPaymentWithNullAmount() {
        Double amount = null;

        Mono<PaymentResultDto> result = paymentService.processPayment(amount);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().equals("Сумма платежа должна быть положительной"))
                .verify();
    }

    @Test
    void processPaymentWithInsufficientFunds() {
        Double amount = INITIAL_BALANCE + 1000.0;

        Mono<PaymentResultDto> result = paymentService.processPayment(amount);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof InsufficientFundsException &&
                                throwable.getMessage().contains("Недостаточно средств") &&
                                throwable.getMessage().contains("Доступно: " + INITIAL_BALANCE) &&
                                throwable.getMessage().contains("требуется: " + amount))
                .verify();
    }

    @Test
    void processPaymentWithExactBalance() {
        Double amount = INITIAL_BALANCE;

        Mono<PaymentResultDto> result = paymentService.processPayment(amount);

        StepVerifier.create(result)
                .assertNext(paymentResult -> {
                    assertThat(paymentResult).isNotNull();
                    assertThat(paymentResult.isSuccess()).isTrue();
                    assertThat(paymentResult.getRemainingBalance()).isEqualTo(0.0);
                })
                .verifyComplete();
    }
}