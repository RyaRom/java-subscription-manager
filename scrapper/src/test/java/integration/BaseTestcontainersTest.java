package integration;

import backend.academy.scrapper.ScrapperApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(classes = ScrapperApplication.class)
@ActiveProfiles("testing")
public class BaseTestcontainersTest {

    @Container
    protected static PostgreSQLContainer<?> postgresContainer =
        new PostgreSQLContainer<>("postgres:17-alpine")
            .withExposedPorts(5432)
            .withDatabaseName("local")
            .withUsername("postgres")
            .withPassword("test");

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        System.out.println("URL IS " + postgresContainer.getJdbcUrl());
    }
}
