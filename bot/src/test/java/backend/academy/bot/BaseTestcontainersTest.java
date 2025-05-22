package backend.academy.bot;

import backend.academy.bot.testcontainers.redis.CacheRedisTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest(classes = BotApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("testing")
public class BaseTestcontainersTest {
    @Container
    protected static final GenericContainer<?> redisContainer = new GenericContainer<>("redis:latest")
        .withExposedPorts(6379)
        .waitingFor(Wait.forListeningPort());

//    @Container
//    protected static KafkaContainer kafkaContainer = new KafkaContainer(
//        DockerImageName.parse("confluentinc/cp-kafka:latest")
//    ).waitingFor(Wait.forListeningPort());

    @LocalServerPort
    protected int port;

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("app.data.redis", CacheRedisTest::redisUrl);
        registry.add("app.data.redis-ex-ms", () -> 1000);

//        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
//        registry.add("spring.kafka.kafka-topics.link-updates.name", () -> TEST_TOPIC);
    }

}
