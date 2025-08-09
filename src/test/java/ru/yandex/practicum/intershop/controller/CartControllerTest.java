package ru.yandex.practicum.intershop.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


public class CartControllerTest extends BasicConfigIntegrationTest{
    @BeforeEach
    void setUpData() {
        insertItem("Товар 1", "Описание товара 1", "image1.jpg", 100, 999.99);
        insertItem("Товар 2", "Описание товара 2", "image2.jpg", 200, 199.50);
        insertCart(1L, 2);
        insertCart(2L, 3);
    }

    @Test
    void getCartModel() throws Exception {
        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("total"))
                .andExpect(model().attributeExists("empty"));
    }

    @Test
    void modifyCartItemPlusItem() throws Exception {
        Integer before = jdbcTemplate.queryForObject("SELECT count FROM cart WHERE item_id = 1", Integer.class);
        assertThat(before).isEqualTo(2);

        mockMvc.perform(post("/cart/items/{id}", 1L)
                        .param("action", "PLUS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart/items"));

        Integer after = jdbcTemplate.queryForObject("SELECT count FROM cart WHERE item_id = 1", Integer.class);
        assertThat(after).isEqualTo(3);
    }

    @Test
    void modifyCartItemMinus() throws Exception {
        Integer before = jdbcTemplate.queryForObject("SELECT count FROM cart WHERE item_id = 2", Integer.class);
        assertThat(before).isEqualTo(3);

        mockMvc.perform(post("/cart/items/{id}", 2L)
                        .param("action", "MINUS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart/items"));

        Integer after = jdbcTemplate.queryForObject("SELECT count FROM cart WHERE item_id = 2", Integer.class);
        assertThat(after).isEqualTo(2);
    }

    @Test
    void modifyCartItemDelete() throws Exception {
        Integer before = jdbcTemplate.queryForObject("SELECT count FROM cart WHERE item_id = 2", Integer.class);
        assertThat(before).isEqualTo(3);

        mockMvc.perform(post("/cart/items/{id}", 2L)
                        .param("action", "DELETE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart/items"));

        Integer after = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM cart WHERE item_id = 2", Integer.class);
        assertThat(after).isEqualTo(0);
    }
}
