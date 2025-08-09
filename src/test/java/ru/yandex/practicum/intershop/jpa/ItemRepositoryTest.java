package ru.yandex.practicum.intershop.jpa;

import ru.yandex.practicum.intershop.configuration.BasicTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.yandex.practicum.intershop.model.Item;
import ru.yandex.practicum.intershop.repository.ItemRepository;

import static org.assertj.core.api.Assertions.assertThat;

public class ItemRepositoryTest extends BasicTestConfiguration {
    @Autowired
    ItemRepository itemRepository;

    @Test
    void searchByTitleOrDescription() {
        itemRepository.save(item("Товар 1", "Описание товара 1", "image1.jpg", 10, 99999.0));
        itemRepository.save(item("Товар 2", "Описание товара 2", "image2.jpg", 5, 79999.0));
        itemRepository.save(item("Другое название", "Другое описание", "image3.jpg", 50, 990.0));

        Page<Item> page = itemRepository.findAllByTitleContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
                "товар", "товар", PageRequest.of(0, 10));

        assertThat(page.getContent().size()).isEqualTo(2);
    }

    private Item item(String t, String d, String f, int c, double p) {
        Item i = new Item();
        i.setTitle(t); i.setDescription(d); i.setFilename(f); i.setCount(c); i.setPrice(p);
        return i;
    }
}
