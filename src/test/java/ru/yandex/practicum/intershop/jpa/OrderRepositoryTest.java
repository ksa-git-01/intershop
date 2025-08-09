package ru.yandex.practicum.intershop.jpa;

import ru.yandex.practicum.intershop.configuration.BasicTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.intershop.model.Order;
import ru.yandex.practicum.intershop.repository.OrderRepository;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderRepositoryTest extends BasicTestConfiguration {
    @Autowired
    OrderRepository orderRepository;

    @Test
    void save() {
        Order o = new Order();
        o = orderRepository.save(o);
        assertThat(orderRepository.findById(o.getId())).isPresent();
    }
}
