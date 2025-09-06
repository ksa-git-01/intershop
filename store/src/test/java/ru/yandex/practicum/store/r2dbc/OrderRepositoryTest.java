package ru.yandex.practicum.store.r2dbc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.store.configuration.BasicTestConfiguration;
import ru.yandex.practicum.store.model.Order;
import ru.yandex.practicum.store.repository.OrderRepository;

import static org.assertj.core.api.Assertions.assertThat;

class OrderRepositoryTest extends BasicTestConfiguration {
    @Autowired
    OrderRepository orderRepository;

    @Test
    void save() {
        Order o = new Order();
        Order saved = orderRepository.save(o).block();

        Order found = orderRepository.findById(saved.getId()).block();
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(saved.getId());
    }
}