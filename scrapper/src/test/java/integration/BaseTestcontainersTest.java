package integration;

import static integration.testcontainers.kafka.BotKafkaClientTest.TEST_TOPIC;

import backend.academy.scrapper.ScrapperApplication;
import integration.testcontainers.redis.CachedLinkRepositoryTest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest(classes = ScrapperApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("testing")
public class BaseTestcontainersTest {
    @Container
    protected static final GenericContainer<?> redisContainer =
            new GenericContainer<>("redis:latest").withExposedPorts(6379).waitingFor(Wait.forListeningPort());

    @Container
    protected static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest")
            .waitingFor(Wait.forListeningPort())
            .withExposedPorts(5432)
            .withDatabaseName("local")
            .withUsername("postgres")
            .withPassword("test")
            .withReuse(true);

    @Container
    protected static KafkaContainer kafkaContainer = new KafkaContainer(
                    DockerImageName.parse("confluentinc/cp-kafka:latest"))
            .waitingFor(Wait.forListeningPort());

    @LocalServerPort
    protected int port;

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        registry.add("app.data.redis", CachedLinkRepositoryTest::redisUrl);
        registry.add("app.data.redis-ex-ms", () -> 1000);

        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.kafka.kafka-topics.link-updates.name", () -> TEST_TOPIC);
    }

    @BeforeAll
    static void liquibase() {
        try (Connection connection = DriverManager.getConnection(
                postgresContainer.getJdbcUrl(), postgresContainer.getUsername(), postgresContainer.getPassword())) {
            Database database =
                    DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

            Liquibase liquibase = new Liquibase("master-test.yaml", new ClassLoaderResourceAccessor(), database);

            liquibase.update();
            connection.commit();

        } catch (SQLException | LiquibaseException e) {
            throw new RuntimeException(e);
        }
    }
}
