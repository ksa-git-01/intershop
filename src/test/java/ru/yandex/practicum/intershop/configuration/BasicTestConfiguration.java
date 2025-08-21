package ru.yandex.practicum.intershop.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public abstract class BasicTestConfiguration {
    static {
        PostgresTestContainer.getInstance();
    }

    @DynamicPropertySource
    static void propertySource(DynamicPropertyRegistry r) {
        var c = PostgresTestContainer.getInstance();
        r.add("spring.r2dbc.url", () -> "r2dbc:postgresql://" + c.getHost() + ":" + c.getFirstMappedPort() + "/" + c.getDatabaseName());
        r.add("spring.r2dbc.username", c::getUsername);
        r.add("spring.r2dbc.password", c::getPassword);
        r.add("spring.sql.init.mode", () -> "never");
    }

    @Autowired
    protected DatabaseClient databaseClient;

    @Autowired
    protected WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        initializeDatabase().block();
    }

    private Mono<Void> initializeDatabase() {
        return databaseClient.sql("DROP TABLE IF EXISTS order_items CASCADE")
                .then()
                .then(databaseClient.sql("DROP TABLE IF EXISTS cart CASCADE").then())
                .then(databaseClient.sql("DROP TABLE IF EXISTS orders CASCADE").then())
                .then(databaseClient.sql("DROP TABLE IF EXISTS item CASCADE").then())
                .then(createTables())
                .then();
    }

    private Mono<Void> createTables() {
        return databaseClient.sql("""
                CREATE TABLE IF NOT EXISTS item (
                  id BIGSERIAL PRIMARY KEY,
                  title VARCHAR(255) NOT NULL,
                  description TEXT NOT NULL,
                  filename VARCHAR(500),
                  count INTEGER NOT NULL,
                  price NUMERIC(10,2) NOT NULL,
                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  updated_at TIMESTAMP
                )
                """).then()
                .then(databaseClient.sql("""
                        CREATE TABLE IF NOT EXISTS cart (
                          id BIGSERIAL PRIMARY KEY,
                          item_id BIGINT,
                          count INTEGER NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP,
                          CONSTRAINT fk_cart_item
                                FOREIGN KEY (item_id)
                                REFERENCES item(id)
                        )
                        """).then())
                .then(databaseClient.sql("""
                        CREATE TABLE IF NOT EXISTS orders (
                          id BIGSERIAL PRIMARY KEY,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP
                        )
                        """).then())
                .then(databaseClient.sql("""
                        CREATE TABLE IF NOT EXISTS order_items (
                          id BIGSERIAL PRIMARY KEY,
                          item_id BIGINT,
                          order_id BIGINT,
                          count INTEGER NOT NULL,
                          price NUMERIC(10,2) NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT fk_order_items_item
                                FOREIGN KEY (item_id)
                                REFERENCES item(id),
                          CONSTRAINT fk_order_items_orders
                                FOREIGN KEY (order_id)
                                REFERENCES orders(id)
                        )
                        """).then());
    }

    protected long insertItem(String title, String description, String filename, int count, double price) {
        return databaseClient.sql("INSERT INTO item(title, description, filename, count, price) VALUES ($1,$2,$3,$4,$5)")
                .bind("$1", title)
                .bind("$2", description)
                .bind("$3", filename)
                .bind("$4", count)
                .bind("$5", price)
                .then()
                .then(databaseClient.sql("SELECT currval(pg_get_serial_sequence('item','id'))")
                        .map(row -> row.get(0, Long.class))
                        .one())
                .block();
    }

    protected void insertCart(long itemId, int count) {
        databaseClient.sql("INSERT INTO cart(item_id, count) VALUES ($1,$2)")
                .bind("$1", itemId)
                .bind("$2", count)
                .then()
                .block();
    }

    protected long insertOrder() {
        return databaseClient.sql("INSERT INTO orders DEFAULT VALUES")
                .then()
                .then(databaseClient.sql("SELECT currval(pg_get_serial_sequence('orders','id'))")
                        .map(row -> row.get(0, Long.class))
                        .one())
                .block();
    }

    protected void insertOrderItem(long orderId, long itemId, int count, double price) {
        databaseClient.sql("INSERT INTO order_items(order_id, item_id, count, price) VALUES ($1,$2,$3,$4)")
                .bind("$1", orderId)
                .bind("$2", itemId)
                .bind("$3", count)
                .bind("$4", price)
                .then()
                .block();
    }

    protected Mono<Integer> getCartCount(long itemId) {
        return databaseClient.sql("SELECT count FROM cart WHERE item_id = $1")
                .bind("$1", itemId)
                .map(row -> row.get("count", Integer.class))
                .one();
    }

    protected Mono<Integer> getCartItemsCount(long itemId) {
        return databaseClient.sql("SELECT COUNT(*) FROM cart WHERE item_id = $1")
                .bind("$1", itemId)
                .map(row -> row.get(0, Integer.class))
                .one();
    }

    protected Mono<Integer> getItemStock(long itemId) {
        return databaseClient.sql("SELECT count FROM item WHERE id = $1")
                .bind("$1", itemId)
                .map(row -> row.get("count", Integer.class))
                .one();
    }

    protected Mono<Integer> getTotalCount(String table) {
        return databaseClient.sql("SELECT COUNT(*) FROM " + table)
                .map(row -> row.get(0, Integer.class))
                .one();
    }

    protected Mono<String> getItemFilename() {
        return databaseClient.sql("SELECT filename FROM item ORDER BY id DESC LIMIT 1")
                .map(row -> row.get("filename", String.class))
                .one();
    }
}