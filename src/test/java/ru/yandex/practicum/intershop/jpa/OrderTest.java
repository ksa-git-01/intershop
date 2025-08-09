package ru.yandex.practicum.intershop.jpa;

import ru.yandex.practicum.intershop.configuration.BasicTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.intershop.model.Item;
import ru.yandex.practicum.intershop.model.Order;
import ru.yandex.practicum.intershop.model.OrderItem;
import ru.yandex.practicum.intershop.repository.ItemRepository;
import ru.yandex.practicum.intershop.repository.OrderItemRepository;
import ru.yandex.practicum.intershop.repository.OrderRepository;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderTest extends BasicTestConfiguration {
    @Autowired ItemRepository itemRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired OrderItemRepository orderItemRepository;

    @Test
    @Transactional
    void createOrderWithItems() {
        Item item = new Item();
        item.setTitle("Товар 1");
        item.setDescription("Описание товара 1");
        item.setFilename("image.jpg");
        item.setCount(5);
        item.setPrice(2999.00);
        item = itemRepository.save(item);

        Order order = new Order();
        order = orderRepository.save(order);

        OrderItem oi = new OrderItem();
        oi.setOrder(order);
        oi.setItem(item);
        oi.setCount(2);
        oi.setPrice(2999.00);
        oi = orderItemRepository.save(oi);

        Order foundOrder = orderRepository.findById(order.getId()).orElseThrow();
        OrderItem foundOi = orderItemRepository.findById(oi.getId()).orElseThrow();

        assertThat(foundOi.getOrder().getId()).isEqualTo(foundOrder.getId());
        assertThat(foundOi.getItem().getId()).isEqualTo(item.getId());
        assertThat(foundOi.getCount()).isEqualTo(2);
        assertThat(foundOi.getPrice()).isEqualTo(2999.00);
}
}
