package integration;

import backend.academy.scrapper.repository.LinkRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TgBotTest extends BaseIntegrationTest {
    @Autowired
    private LinkRepository linkRepository;

    @AfterEach
    void tearDown() {
        linkRepository.drop();
    }

    @Test
    void register() {
        webTestClient.post()
            .uri("/tg-chat/{chatId}", 1)
            .exchange()
            .expectStatus()
            .isOk();
    }

    @Test
    void delete() {
        webTestClient.delete()
            .uri("/tg-chat/{chatId}", 1)
            .exchange()
            .expectStatus()
            .isOk();
    }
}
