package ru.yandex.practicum.store.configuration;

import redis.embedded.RedisServer;
import java.io.IOException;

public class EmbeddedRedisTestContainer {
    private static RedisServer redisServer;
    private static final int REDIS_PORT = 6370;

    public static synchronized void start() {
        if (redisServer == null || !redisServer.isActive()) {
            try {
                redisServer = new RedisServer(REDIS_PORT);
                redisServer.start();
            } catch (IOException e) {
                throw new RuntimeException("Failed to start embedded Redis", e);
            }
        }
    }

    public static synchronized void stop() throws IOException {
        if (redisServer != null && redisServer.isActive()) {
            redisServer.stop();
        }
    }

    public static int getPort() {
        return REDIS_PORT;
    }
}