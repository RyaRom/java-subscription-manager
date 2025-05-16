package integration;

import backend.academy.configuration.EnvType;
import backend.academy.scrapper.repository.links.LinkRepository;
import backend.academy.scrapper.repository.links.ORMLinkRepository;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

@EnableAutoConfiguration
@ContextConfiguration(classes = BaseIntegrationTest.TestConfig.class)
public class BaseIntegrationTest extends BaseTestcontainersTest {
    private static final ObjectMapper mapper = new ObjectMapper();

    protected WebTestClient webTestClient;

    protected String apiUrl;

    protected static String asJsonString(final Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    public void setup() {
        apiUrl = "http://localhost:" + port + "/scrapper/api";
        webTestClient = WebTestClient.bindToServer().baseUrl(apiUrl).build();
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public LinkRepository linkRepository(SessionFactory sessionFactory) {
            return new ORMLinkRepository(EnvType.TEST, sessionFactory);
        }
    }
}
