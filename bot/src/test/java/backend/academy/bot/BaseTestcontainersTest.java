package backend.academy.bot;

import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.clients.ScrapperClientCached;
import backend.academy.bot.clients.ScrapperHttpClient;
import backend.academy.bot.clients.ScrapperPublisher;
import backend.academy.bot.clients.ScrapperPublisherCached;
import backend.academy.bot.config.ClientsProps;
import backend.academy.bot.config.DataProps;
import backend.academy.bot.testcontainers.redis.CacheRedisTest;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(classes = BotApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("testing")
@ContextConfiguration(classes = BaseTestcontainersTest.IntegrationTestConfig.class)
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

    @Configuration
    public static class IntegrationTestConfig {
        @Bean
        public ScrapperHttpClient scrapperHttpClient() {
            return new ScrapperHttpClient(Mockito.mock(WebClient.class));
        }

        @Bean
        public ScrapperClient scrapperClient() {
            return new ScrapperClientCached(
                Mockito.mock(DataProps.class),
                Mockito.mock(ScrapperHttpClient.class),
                Mockito.mock(RedisReactiveCommands.class));
        }

        @Bean
        public ScrapperPublisher scrapperPublisher() {
            return new ScrapperPublisherCached(
                Mockito.mock(ScrapperHttpClient.class),
                Mockito.mock(RedisReactiveCommands.class));
        }
    }
}
