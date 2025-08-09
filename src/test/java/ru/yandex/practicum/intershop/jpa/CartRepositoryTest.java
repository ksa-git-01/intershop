package ru.yandex.practicum.intershop.jpa;

import ru.yandex.practicum.intershop.configuration.BasicTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.intershop.model.Cart;
import ru.yandex.practicum.intershop.model.Item;
import ru.yandex.practicum.intershop.repository.CartRepository;
import ru.yandex.practicum.intershop.repository.ItemRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class CartRepositoryTest extends BasicTestConfiguration {
    @Autowired
    CartRepository cartRepository;
    @Autowired
    ItemRepository itemRepository;

    @Test
    void findByItemId() {
        Item item = new Item();
        item.setTitle("Товар 1"); item.setDescription("Описание товара 1");
        item.setFilename("image.jpg");
        item.setCount(100);
        item.setPrice(199.5);
        item = itemRepository.save(item);

        Cart c = new Cart();
        c.setItem(item);
        c.setCount(3);
        cartRepository.save(c);

        Optional<Cart> found = cartRepository.findByItemId(item.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCount()).isEqualTo(3);
    }
}
