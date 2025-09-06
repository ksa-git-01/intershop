package ru.yandex.practicum.store.r2dbc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import ru.yandex.practicum.store.configuration.BasicTestConfiguration;
import ru.yandex.practicum.store.model.Item;
import ru.yandex.practicum.store.repository.ItemRepository;

import static org.assertj.core.api.Assertions.assertThat;

class ItemRepositoryTest extends BasicTestConfiguration {
    @Autowired
    ItemRepository itemRepository;

    @Test
    void searchByTitleOrDescription() {
        Item item1 = item("Товар 1", "Описание товара 1", "image1.jpg", 10, 99999.0);
        Item item2 = item("Товар 2", "Описание товара 2", "image2.jpg", 5, 79999.0);
        Item item3 = item("Другое название", "Другое описание", "image3.jpg", 50, 990.0);

        itemRepository.save(item1).block();
        itemRepository.save(item2).block();
        itemRepository.save(item3).block();

        var results = itemRepository.findAllByTitleContainsIgnoreCaseOrDescriptionContainsIgnoreCase(
                        "товар", "товар", PageRequest.of(0, 10))
                .collectList()
                .block();

        assertThat(results).hasSize(2);
    }

    private Item item(String t, String d, String f, int c, double p) {
        Item i = new Item();
        i.setTitle(t);
        i.setDescription(d);
        i.setFilename(f);
        i.setCount(c);
        i.setPrice(p);
        return i;
    }
}