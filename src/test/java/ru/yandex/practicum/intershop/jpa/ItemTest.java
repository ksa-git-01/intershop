package ru.yandex.practicum.intershop.jpa;

import ru.yandex.practicum.intershop.configuration.BasicTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.intershop.model.Item;
import ru.yandex.practicum.intershop.repository.ItemRepository;

import static org.assertj.core.api.Assertions.assertThat;

public class ItemTest extends BasicTestConfiguration {
    @Autowired ItemRepository itemRepository;

    @Test
    @Transactional
    void createNew() {
        Item i = new Item();
        i.setTitle("Товар 1");
        i.setDescription("Описание 1");
        i.setFilename("image.jpg");
        i.setCount(10);
        i.setPrice(1999.99);
        Item saved = itemRepository.save(i);

        Item found = itemRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getTitle()).isEqualTo("Товар 1");
        assertThat(found.getDescription()).isEqualTo("Описание 1");
        assertThat(found.getFilename()).isEqualTo("image.jpg");
        assertThat(found.getCount()).isEqualTo(10);
        assertThat(found.getPrice()).isEqualTo(1999.99);
    }
}
