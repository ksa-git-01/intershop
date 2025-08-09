package ru.yandex.practicum.intershop.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@Testcontainers
@Sql(scripts = "/test-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@AutoConfigureMockMvc
public class CartControllerTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.9-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.sql.init.mode", () -> "never");
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUpData() {
        jdbcTemplate.update("DELETE FROM order_items");
        jdbcTemplate.update("DELETE FROM orders");
        jdbcTemplate.update("DELETE FROM cart");
        jdbcTemplate.update("DELETE FROM item");

        jdbcTemplate.update("INSERT INTO item(title, description, filename, count, price) VALUES (?,?,?,?,?)",
                "Товар 1", "Описание товара 1", "image1.jpg", 100, 999.99);
        jdbcTemplate.update("INSERT INTO item(title, description, filename, count, price) VALUES (?,?,?,?,?)",
                "Товар 2", "Описание товара 2", "image2.jpg", 200, 199.50);

        jdbcTemplate.update("INSERT INTO cart(item_id, count) VALUES (?,?)", 1L, 2);
        jdbcTemplate.update("INSERT INTO cart(item_id, count) VALUES (?,?)", 2L, 3);
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
