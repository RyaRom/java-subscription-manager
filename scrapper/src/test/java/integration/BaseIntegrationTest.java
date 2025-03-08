package integration;

import backend.academy.scrapper.ScrapperApplication;
import backend.academy.scrapper.repository.InMemoryLinkRepository;
import backend.academy.scrapper.repository.LinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

@EnableAutoConfiguration
@SpringBootTest(classes = ScrapperApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-test.yaml")
@ContextConfiguration(classes = BaseIntegrationTest.TestConfig.class)
public class BaseIntegrationTest {
    private static final ObjectMapper mapper = new ObjectMapper();

    protected WebTestClient webTestClient;

    @LocalServerPort
    protected int port;

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
        public LinkRepository linkRepository() {
            return new InMemoryLinkRepository();
        }
    }
}
