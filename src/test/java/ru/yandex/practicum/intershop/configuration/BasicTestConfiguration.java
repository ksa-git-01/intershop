package ru.yandex.practicum.intershop.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/test-schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class BasicTestConfiguration {
    static {
        PostgresTestContainer.getInstance();
    }

    @DynamicPropertySource
    static void propertySource(DynamicPropertyRegistry r) {
        var c = PostgresTestContainer.getInstance();
        r.add("spring.datasource.url", c::getJdbcUrl);
        r.add("spring.datasource.username", c::getUsername);
        r.add("spring.datasource.password", c::getPassword);
        r.add("spring.sql.init.mode", () -> "never");
    }

    @Autowired
    protected JdbcTemplate jdbcTemplate;
    @Autowired
    protected MockMvc mockMvc;

    @BeforeEach
    void truncateAll() {
        jdbcTemplate.update("TRUNCATE TABLE order_items, orders, cart, item RESTART IDENTITY CASCADE");
    }

    protected long insertItem(String title, String description, String filename, int count, double price) {
        jdbcTemplate.update("INSERT INTO item(title, description, filename, count, price) VALUES (?,?,?,?,?)",
                title, description, filename, count, price);
        return jdbcTemplate.queryForObject("SELECT currval(pg_get_serial_sequence('item','id'))", Long.class);
    }

    protected void insertCart(long itemId, int count) {
        jdbcTemplate.update("INSERT INTO cart(item_id, count) VALUES (?,?)", itemId, count);
    }

    protected long insertOrder() {
        jdbcTemplate.update("INSERT INTO orders DEFAULT VALUES");
        return jdbcTemplate.queryForObject("SELECT currval(pg_get_serial_sequence('orders','id'))", Long.class);
    }

    protected void insertOrderItem(long orderId, long itemId, int count, double price) {
        jdbcTemplate.update("INSERT INTO order_items(order_id, item_id, count, price) VALUES (?,?,?,?)",
                orderId, itemId, count, price);
    }
}
