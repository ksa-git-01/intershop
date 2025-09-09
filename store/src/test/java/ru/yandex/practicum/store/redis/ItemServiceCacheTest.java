package ru.yandex.practicum.store.redis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import ru.yandex.practicum.store.configuration.BasicTestConfiguration;
import ru.yandex.practicum.store.model.Item;
import ru.yandex.practicum.store.service.ItemService;

import static org.assertj.core.api.Assertions.assertThat;

public class ItemServiceCacheTest extends BasicTestConfiguration {

    @Autowired
    private ItemService itemService;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void findItemById() {
        // Создаем товар
        long itemId = insertItem("Товар 1", "Описание товара 1", "testimage.jpg", 10, 99.99);

        // Делаем вызов
        Item firstCall = itemService.findItemById(itemId).block();

        // Проверяем, что данные есть в кеше
        var cache = cacheManager.getCache("items");
        assertThat(cache).isNotNull();
        assertThat(cache.get(itemId)).isNotNull();
    }

    @Test
    void updateItemStock() {
        // Создаем товар
        long itemId = insertItem("Товар 1", "Описание товара 1", "testimage.jpg", 10, 99.99);

        // Делаем вызов
        Item cachedItem = itemService.findItemById(itemId).block();
        assertThat(cachedItem.getCount()).isEqualTo(10);

        // Проверяем, что данные есть в кеше
        var cache = cacheManager.getCache("items");
        assertThat(cache.get(itemId)).isNotNull();

        // Обновляем остатки
        itemService.updateItemStock(itemId, 3).block();

        // Проверяем что кэш товара инвалидирован
        assertThat(cache.get(itemId)).isNull();
    }

    @Test
    void addItem() {
        // Создаем товар
        long existingItemId = insertItem("Товар 1", "Описание товара 1", "testimage.jpg", 10, 99.99);

        // Загружаем существующий товар в кеш
        Item existingItem = itemService.findItemById(existingItemId).block();
        assertThat(existingItem).isNotNull();

        // Проверяем, что данные есть в кеше
        var cache = cacheManager.getCache("items");
        assertThat(cache.get(existingItemId)).isNotNull();

        // Добавляем новый товар
        Long newItemId = itemService.addItem("Товар 2", "Описание товара 2", 3, 30.0, "testimage.jpg").block();

        // Существующий товар должен остаться в кеше
        cache = cacheManager.getCache("items");
        assertThat(cache.get(existingItemId)).isNotNull();
    }
}