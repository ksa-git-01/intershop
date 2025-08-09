package ru.yandex.practicum.intershop.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MainControllerTest extends BasicConfigIntegrationTest {

    @BeforeEach
    void setUpData() {
        insertItem("Товар 1", "Описание товара 1", "image1.jpg", 100, 999.99);
        insertItem("Товар 2", "Описание товара 2", "image2.jpg", 200, 199.50);
    }

    @Test
    void rootRedirectsToMain() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main/items"));
    }

    @Test
    void getMainItems() throws Exception {
        mockMvc.perform(get("/main/items")
                        .param("search", "")
                        .param("sort", "NO")
                        .param("pageNumber", "0")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attributeExists("items", "search", "sort", "paging"));
    }

    @Test
    void modifyCartItemPlusItem() throws Exception {
        long itemId = insertItem("Товар 3", "Описание товара 3", "image3.jpg", 5, 2999.00);
        insertCart(itemId, 1);

        Integer before = jdbcTemplate.queryForObject("SELECT count FROM cart WHERE item_id=?", Integer.class, itemId);
        assertThat(before).isEqualTo(1);

        mockMvc.perform(post("/main/items/{id}", itemId).param("action", "PLUS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main/items"));

        Integer after = jdbcTemplate.queryForObject("SELECT count FROM cart WHERE item_id=?", Integer.class, itemId);
        assertThat(after).isEqualTo(2);
    }

    @Test
    void modifyCartItemMinusItem() throws Exception {
        long itemId = insertItem("Товар 3", "Описание товара 3", "image3.jpg", 5, 2999.00);
        insertCart(itemId, 2);

        Integer before = jdbcTemplate.queryForObject("SELECT count FROM cart WHERE item_id=?", Integer.class, itemId);
        assertThat(before).isEqualTo(2);

        mockMvc.perform(post("/main/items/{id}", itemId).param("action", "MINUS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main/items"));

        Integer after = jdbcTemplate.queryForObject("SELECT count FROM cart WHERE item_id=?", Integer.class, itemId);
        assertThat(after).isEqualTo(1);
    }

    @Test
    void modifyCartItemDeleteItem() throws Exception {
        long itemId = insertItem("Товар 3", "Описание товара 3", "image3.jpg", 5, 2999.00);
        insertCart(itemId, 2);

        Integer before = jdbcTemplate.queryForObject("SELECT count FROM cart WHERE item_id=?", Integer.class, itemId);
        assertThat(before).isEqualTo(2);

        mockMvc.perform(post("/main/items/{id}", itemId).param("action", "DELETE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main/items"));

        Integer after = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM cart WHERE item_id=?", Integer.class, itemId);
        assertThat(after).isEqualTo(0);
    }
}

