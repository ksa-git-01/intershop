package ru.yandex.practicum.store.r2dbc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.store.configuration.BasicTestConfiguration;
import ru.yandex.practicum.store.model.Cart;
import ru.yandex.practicum.store.model.Item;
import ru.yandex.practicum.store.repository.CartRepository;
import ru.yandex.practicum.store.repository.ItemRepository;

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
