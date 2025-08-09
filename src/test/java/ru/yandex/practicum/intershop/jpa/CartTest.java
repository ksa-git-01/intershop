package ru.yandex.practicum.intershop.jpa;

import ru.yandex.practicum.intershop.configuration.BasicTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.intershop.model.Cart;
import ru.yandex.practicum.intershop.model.Item;
import ru.yandex.practicum.intershop.repository.CartRepository;
import ru.yandex.practicum.intershop.repository.ItemRepository;

import static org.assertj.core.api.Assertions.assertThat;

public class CartTest extends BasicTestConfiguration {
    @Autowired ItemRepository itemRepository;
    @Autowired CartRepository cartRepository;

    @Test
    @Transactional
    void createNewItemAndCart() {
        Item item = new Item();
        item.setTitle("Товар 1");
        item.setDescription("Описание товара 1");
        item.setFilename("image.jpg");
        item.setCount(100);
        item.setPrice(199.50);
        item = itemRepository.save(item);

        Cart cart = new Cart();
        cart.setItem(item);
        cart.setCount(3);
        Cart saved = cartRepository.save(cart);

        Cart found = cartRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getItem().getId()).isEqualTo(item.getId());
        assertThat(found.getCount()).isEqualTo(3);
    }
}
