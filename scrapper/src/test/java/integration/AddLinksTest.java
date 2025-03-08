package integration;

import backend.academy.dto.AddLinkRequest;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.dto.Link;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.assertThat;

public class AddLinksTest extends BaseIntegrationTest {
    private final AddLinkRequest badLink =
        AddLinkRequest.builder()
            .link("https://google.com")
            .build();
    private final AddLinkRequest githubLink =
        AddLinkRequest.builder()
            .link("https://github.com/RyaRom/HackChangeHackathon2024")
            .build();
    private final AddLinkRequest stackOverflowLink =
        AddLinkRequest.builder()
            .link("https://stackoverflow.com/questions/1732348/regex-match-open-tags-except-xhtml-self-contained-tags")
            .build();
    @Autowired
    private LinkRepository linkRepository;

    @AfterEach
    void tearDown() {
        linkRepository.drop();
    }

    @Test
    void addBadLink() {
        webTestClient.post()
            .uri("/links")
            .header("Content-Type", "application/json")
            .header("Tg-Chat-Id", "1")
            .bodyValue(asJsonString(badLink))
            .exchange()
            .expectStatus().is4xxClientError();

        webTestClient.post()
            .uri("/links")
            .header("Content-Type", "application/json")
            .bodyValue(asJsonString(githubLink))
            .exchange()
            .expectStatus().is4xxClientError();
    }

    @Test
    void addGithubLink() {
        webTestClient.post()
            .uri("/links")
            .header("Content-Type", "application/json")
            .header("Tg-Chat-Id", "1")
            .bodyValue(asJsonString(githubLink))
            .exchange()
            .expectStatus().isOk();

        assertThat(linkRepository.findAll())
            .singleElement()
            .satisfies(link -> {
                assertThat(link.getUrl()).isEqualTo("https://github.com/RyaRom/HackChangeHackathon2024");
                assertThat(link.getGithubInfo()).isNotNull();
                assertThat(link.getGithubInfo().owner()).isEqualTo("RyaRom");
                assertThat(link.getGithubInfo().repo()).isEqualTo("HackChangeHackathon2024");
                assertThat(link.getChatIds()).contains(1L);
                assertThat(link.getLinkType()).isEqualTo(Link.Type.GITHUB);
                assertThat(link.getStackOverflowInfo()).isNull();
            });
    }

    @Test
    void addStackOverflowLink() {
        webTestClient.post()
            .uri("/links")
            .header("Content-Type", "application/json")
            .header("Tg-Chat-Id", "1")
            .bodyValue(asJsonString(stackOverflowLink))
            .exchange()
            .expectStatus().isOk();

        assertThat(linkRepository.findAll())
            .singleElement()
            .satisfies(link -> {
                assertThat(link.getUrl()).isEqualTo("https://stackoverflow.com/questions/1732348/regex-match-open-tags-except-xhtml-self-contained-tags");
                assertThat(link.getGithubInfo()).isNull();
                assertThat(link.getStackOverflowInfo()).isNotNull();
                assertThat(link.getStackOverflowInfo().questionId()).isEqualTo(1732348);
                assertThat(link.getChatIds()).contains(1L);
                assertThat(link.getLinkType()).isEqualTo(Link.Type.STACK_OVERFLOW);
            });
    }
}
