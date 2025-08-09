package ru.yandex.practicum.intershop.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.intershop.configuration.BasicTestConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class OrderControllerTest extends BasicTestConfiguration {
    private long firstItemId;
    private long secondItemId;

    @BeforeEach
    void setUpData() {
        firstItemId = insertItem("Товар 1", "Описание товара 1", "image1.jpg", 100, 999.99);
        secondItemId = insertItem("Товар 2", "Описание товара 2", "image2.jpg", 200, 199.50);
    }

    @Test
    void getOrders() throws Exception {
        long orderId = insertOrder();
        insertOrderItem(orderId, firstItemId, 2, 1999.98);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("orders"));
    }

    @Test
    void getOrderById() throws Exception {
        long orderId = insertOrder();
        insertOrderItem(orderId, firstItemId, 1, 1999.98);

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attribute("newOrder", false));
    }

    @Test
    void buy() throws Exception {
        insertCart(firstItemId, 2);
        insertCart(secondItemId, 3);

        Integer stockFirstBefore = jdbcTemplate.queryForObject(
                "SELECT count FROM item WHERE id=?", Integer.class, firstItemId);
        Integer stockSecondBefore = jdbcTemplate.queryForObject(
                "SELECT count FROM item WHERE id=?", Integer.class, secondItemId);

        mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/orders/*?newOrder=true"));

        Integer ordersCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders", Integer.class);
        Integer orderItemsCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM order_items", Integer.class);
        Integer cartAfter = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM cart", Integer.class);
        Integer stockFirstAfter = jdbcTemplate.queryForObject(
                "SELECT count FROM item WHERE id=?", Integer.class, firstItemId);
        Integer stockSecondAfter = jdbcTemplate.queryForObject(
                "SELECT count FROM item WHERE id=?", Integer.class, secondItemId);

        assertThat(ordersCount).isEqualTo(1);
        assertThat(orderItemsCount).isEqualTo(2);
        assertThat(cartAfter).isZero();
        assertThat(stockFirstAfter).isEqualTo(stockFirstBefore - 2);
        assertThat(stockSecondAfter).isEqualTo(stockSecondBefore - 3);
    }
}
