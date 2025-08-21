package ru.yandex.practicum.intershop.r2dbc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.intershop.configuration.BasicTestConfiguration;
import ru.yandex.practicum.intershop.model.Cart;
import ru.yandex.practicum.intershop.model.Item;
import ru.yandex.practicum.intershop.repository.CartRepository;
import ru.yandex.practicum.intershop.repository.ItemRepository;

import static org.assertj.core.api.Assertions.assertThat;

class CartTest extends BasicTestConfiguration {
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    CartRepository cartRepository;

    @Test
    void createNewItemAndCart() {
        Item item = new Item();
        item.setTitle("Товар 1");
        item.setDescription("Описание товара 1");
        item.setFilename("image.jpg");
        item.setCount(100);
        item.setPrice(199.50);

        Item savedItem = itemRepository.save(item).block();

        Cart cart = new Cart();
        cart.setItemId(savedItem.getId());
        cart.setCount(3);

        Cart savedCart = cartRepository.save(cart).block();

        Cart found = cartRepository.findById(savedCart.getId()).block();
        assertThat(found).isNotNull();
        assertThat(found.getItemId()).isEqualTo(savedItem.getId());
        assertThat(found.getCount()).isEqualTo(3);
    }
}