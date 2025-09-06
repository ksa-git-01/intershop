package ru.yandex.practicum.store.configuration;

import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresTestContainer extends PostgreSQLContainer<PostgresTestContainer> {
    private static final String IMAGE = "postgres:16-alpine";
    private static PostgresTestContainer container;

    private PostgresTestContainer() { super(IMAGE); }

    public static synchronized PostgresTestContainer getInstance() {
        if (container == null) {
            container = new PostgresTestContainer()
                    .withDatabaseName("test")
                    .withUsername("test")
                    .withPassword("test");
            container.start();
        }
        return container;
    }
}