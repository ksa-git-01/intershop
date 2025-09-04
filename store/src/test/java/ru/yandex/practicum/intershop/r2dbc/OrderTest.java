package ru.yandex.practicum.intershop.r2dbc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.intershop.configuration.BasicTestConfiguration;
import ru.yandex.practicum.intershop.model.Item;
import ru.yandex.practicum.intershop.model.Order;
import ru.yandex.practicum.intershop.model.OrderItem;
import ru.yandex.practicum.intershop.repository.ItemRepository;
import ru.yandex.practicum.intershop.repository.OrderItemRepository;
import ru.yandex.practicum.intershop.repository.OrderRepository;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTest extends BasicTestConfiguration {
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    OrderItemRepository orderItemRepository;

    @Test
    void createOrderWithItems() {
        Item item = new Item();
        item.setTitle("Товар 1");
        item.setDescription("Описание товара 1");
        item.setFilename("image.jpg");
        item.setCount(5);
        item.setPrice(2999.00);

        Item savedItem = itemRepository.save(item).block();

        Order order = new Order();
        Order savedOrder = orderRepository.save(order).block();

        OrderItem oi = new OrderItem();
        oi.setOrderId(savedOrder.getId());
        oi.setItemId(savedItem.getId());
        oi.setCount(2);
        oi.setPrice(2999.00);

        OrderItem savedOi = orderItemRepository.save(oi).block();

        Order foundOrder = orderRepository.findById(savedOrder.getId()).block();
        OrderItem foundOi = orderItemRepository.findById(savedOi.getId()).block();

        assertThat(foundOi).isNotNull();
        assertThat(foundOi.getOrderId()).isEqualTo(foundOrder.getId());
        assertThat(foundOi.getItemId()).isEqualTo(savedItem.getId());
        assertThat(foundOi.getCount()).isEqualTo(2);
        assertThat(foundOi.getPrice()).isEqualTo(2999.00);
    }
}