package backend.academy.bot;

import backend.academy.bot.testcontainers.redis.CacheRedisTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(classes = BotApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("testing")
public class BaseTestcontainersTest {
    @Container
    protected static final GenericContainer<?> redisContainer =
            new GenericContainer<>("redis:latest").withExposedPorts(6379).waitingFor(Wait.forListeningPort());

    @LocalServerPort
    protected int port;

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("app.data.redis", CacheRedisTest::redisUrl);
        registry.add("app.data.redis-ex-ms", () -> 1000);
    }
}
