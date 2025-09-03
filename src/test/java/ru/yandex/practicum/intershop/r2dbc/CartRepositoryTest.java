package ru.yandex.practicum.intershop.r2dbc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.intershop.configuration.BasicTestConfiguration;
import ru.yandex.practicum.intershop.model.Cart;
import ru.yandex.practicum.intershop.model.Item;
import ru.yandex.practicum.intershop.repository.CartRepository;
import ru.yandex.practicum.intershop.repository.ItemRepository;

import static org.assertj.core.api.Assertions.assertThat;

public class CartRepositoryTest extends BasicTestConfiguration {
    @Autowired
    CartRepository cartRepository;
    @Autowired
    ItemRepository itemRepository;

    @Test
    void findByItemId() {
        Item item = new Item();
        item.setTitle("Товар 1");
        item.setDescription("Описание товара 1");
        item.setFilename("image.jpg");
        item.setCount(100);
        item.setPrice(199.5);

        Item savedItem = itemRepository.save(item).block();

        Cart cart = new Cart();
        cart.setItemId(savedItem.getId());
        cart.setCount(3);

        cartRepository.save(cart).block();

        Cart found = cartRepository.findByItemId(savedItem.getId()).block();
        assertThat(found).isNotNull();
        assertThat(found.getCount()).isEqualTo(3);
    }
}
