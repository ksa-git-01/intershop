package ru.yandex.practicum.store.redis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.store.client.PaymentClient;
import ru.yandex.practicum.store.client.model.PostPayment200Response;
import ru.yandex.practicum.store.client.model.PostPaymentRequest;
import ru.yandex.practicum.store.configuration.BasicTestConfiguration;
import ru.yandex.practicum.store.model.Item;
import ru.yandex.practicum.store.service.ItemService;
import ru.yandex.practicum.store.service.OrderService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class OrderServiceCacheTest extends BasicTestConfiguration {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private PaymentClient paymentClient;

    @Test
    void buyWithInvalidatingCache() {
        when(paymentClient.postPayment(any(PostPaymentRequest.class)))
                .thenReturn(Mono.just(new PostPayment200Response()
                        .success(true)
                        .remainingBalance(500.0)));

        // Создаем товары
        long itemId1 = insertItem("Товар 1", "Описание товара 1", "itemimage1.jpg", 10, 100.0);
        long itemId2 = insertItem("Товар 2", "Описание товара 2", "itemimage2.jpg", 5, 50.0);

        // Загружаем товары в кеш
        Item cachedItem1 = itemService.findItemById(itemId1).block();
        Item cachedItem2 = itemService.findItemById(itemId2).block();
        assertThat(cachedItem1.getCount()).isEqualTo(10);
        assertThat(cachedItem2.getCount()).isEqualTo(5);

        // Проверяем что товары есть в кеше
        var cache = cacheManager.getCache("items");
        assertThat(cache.get(itemId1)).isNotNull();
        assertThat(cache.get(itemId2)).isNotNull();

        // Добавляем товары в корзину
        insertCart(itemId1, 3);
        insertCart(itemId2, 2);

        // Совершаем покупку
        Long orderId = orderService.buy().block();
        assertThat(orderId).isNotNull();

        // Проверяем что кеш товаров инвалидирован после покупки
        assertThat(cache.get(itemId1)).isNull();
        assertThat(cache.get(itemId2)).isNull();

        // Проверяем что остатки товаров обновились
        Item updatedItem1 = itemService.findItemById(itemId1).block();
        Item updatedItem2 = itemService.findItemById(itemId2).block();

        assertThat(updatedItem1.getCount()).isEqualTo(7);
        assertThat(updatedItem2.getCount()).isEqualTo(3);

        // Проверяем что корзина очищена
        Integer cartItemsCount = getTotalCount("cart").block();
        assertThat(cartItemsCount).isEqualTo(0);
    }

    @Test
    void buyShouldNotAffectCacheForNonPurchasedItems() {
        when(paymentClient.postPayment(any(PostPaymentRequest.class)))
                .thenReturn(Mono.just(new PostPayment200Response()
                        .success(true)
                        .remainingBalance(500.0)));

        // Создаем товары
        long purchasedItemId = insertItem("Купленный товар", "Описание", "purchased.jpg", 10, 100.0);
        long nonPurchasedItemId = insertItem("Некупленный товар", "Описание", "non_purchased.jpg", 5, 50.0);

        // Загружаем оба товара в кеш
        Item purchasedItem = itemService.findItemById(purchasedItemId).block();
        Item nonPurchasedItem = itemService.findItemById(nonPurchasedItemId).block();
        assertThat(purchasedItem.getCount()).isEqualTo(10);
        assertThat(nonPurchasedItem.getCount()).isEqualTo(5);

        // Проверяем что товары есть в кеше
        var cache = cacheManager.getCache("items");
        assertThat(cache.get(purchasedItemId)).isNotNull();
        assertThat(cache.get(nonPurchasedItemId)).isNotNull();

        // Добавляем в корзину только один товар
        insertCart(purchasedItemId, 2);

        // Совершаем покупку
        orderService.buy().block();

        // Проверяем что кеш купленного товара инвалидирован
        assertThat(cache.get(purchasedItemId)).isNull();

        // Проверяем что кеш некупленного товара остался
        assertThat(cache.get(nonPurchasedItemId)).isNotNull();

        // Проверяем что купленный товар обновился
        Item updatedPurchasedItem = itemService.findItemById(purchasedItemId).block();
        assertThat(updatedPurchasedItem.getCount()).isEqualTo(8);
    }
}