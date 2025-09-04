package ru.yandex.practicum.intershop.r2dbc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.intershop.configuration.BasicTestConfiguration;
import ru.yandex.practicum.intershop.model.Item;
import ru.yandex.practicum.intershop.repository.ItemRepository;

import static org.assertj.core.api.Assertions.assertThat;

class ItemTest extends BasicTestConfiguration {
    @Autowired
    ItemRepository itemRepository;

    @Test
    void createNew() {
        Item i = new Item();
        i.setTitle("Товар 1");
        i.setDescription("Описание 1");
        i.setFilename("image.jpg");
        i.setCount(10);
        i.setPrice(1999.99);

        Item saved = itemRepository.save(i).block();
        Item found = itemRepository.findById(saved.getId()).block();

        assertThat(found).isNotNull();
        assertThat(found.getTitle()).isEqualTo("Товар 1");
        assertThat(found.getDescription()).isEqualTo("Описание 1");
        assertThat(found.getFilename()).isEqualTo("image.jpg");
        assertThat(found.getCount()).isEqualTo(10);
        assertThat(found.getPrice()).isEqualTo(1999.99);
    }
}