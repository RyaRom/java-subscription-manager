package integration.api;

import static backend.academy.configuration.CustomHeaders.TG_CHAT_ID;
import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinkResponse;
import backend.academy.dto.RemoveLinkRequest;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.dto.Link;
import integration.BaseIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

public class AddLinksTest extends BaseIntegrationTest {
    private final AddLinkRequest badLink =
            AddLinkRequest.builder().link("https://google.com").build();
    private final AddLinkRequest githubLink = AddLinkRequest.builder()
            .link("https://github.com/RyaRom/HackChangeHackathon2024")
            .build();
    private final AddLinkRequest stackOverflowLink = AddLinkRequest.builder()
            .link("https://stackoverflow.com/questions/1732348/regex-match-open-tags-except-xhtml-self-contained-tags")
            .build();

    @Autowired
    private LinkRepository linkRepository;

    @AfterEach
    void tearDown() {
        linkRepository.dropForTest();
    }

    @Test
    void addBadLink() {
        webTestClient
                .post()
                .uri("/links")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(TG_CHAT_ID, "1")
                .bodyValue(asJsonString(badLink))
                .exchange()
                .expectStatus()
                .is4xxClientError();

        webTestClient
                .post()
                .uri("/links")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .bodyValue(asJsonString(githubLink))
                .exchange()
                .expectStatus()
                .is4xxClientError();
    }

    @Test
    void addGithubLink() {
        webTestClient
                .post()
                .uri("/links")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(TG_CHAT_ID, "1")
                .bodyValue(asJsonString(githubLink))
                .exchange()
                .expectStatus()
                .isOk();

        assertThat(linkRepository.findAll()).singleElement().satisfies(link -> {
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
        webTestClient
                .post()
                .uri("/links")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(TG_CHAT_ID, "1")
                .bodyValue(asJsonString(stackOverflowLink))
                .exchange()
                .expectStatus()
                .isOk();

        assertThat(linkRepository.findAll()).singleElement().satisfies(link -> {
            assertThat(link.getUrl())
                    .isEqualTo(
                            "https://stackoverflow.com/questions/1732348/regex-match-open-tags-except-xhtml-self-contained-tags");
            assertThat(link.getGithubInfo()).isNull();
            assertThat(link.getStackOverflowInfo()).isNotNull();
            assertThat(link.getStackOverflowInfo().questionId()).isEqualTo(1732348);
            assertThat(link.getChatIds()).contains(1L);
            assertThat(link.getLinkType()).isEqualTo(Link.Type.STACK_OVERFLOW);
        });
    }

    @Test
    void getLinksSameId() {
        webTestClient
                .post()
                .uri("/links")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(TG_CHAT_ID, "1")
                .bodyValue(asJsonString(stackOverflowLink))
                .exchange()
                .expectStatus()
                .isOk();
        webTestClient
                .post()
                .uri("/links")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(TG_CHAT_ID, "1")
                .bodyValue(asJsonString(githubLink))
                .exchange()
                .expectStatus()
                .isOk();
        var links = webTestClient
                .get()
                .uri("/links")
                .header(TG_CHAT_ID, "1")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(ListLinkResponse.class)
                .returnResult()
                .getResponseBody()
                .links();

        assertThat(links)
                .hasSize(2)
                .map(LinkResponse::url)
                .containsExactlyInAnyOrder(githubLink.getLink(), stackOverflowLink.getLink());
        assertThat(linkRepository.findAll())
                .hasSize(2)
                .allMatch(link ->
                        link.getChatIds().size() == 1 && link.getChatIds().contains(1L));
    }

    @Test
    void getLinksSameUrl() {
        webTestClient
                .post()
                .uri("/links")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(TG_CHAT_ID, "1")
                .bodyValue(asJsonString(stackOverflowLink))
                .exchange()
                .expectStatus()
                .isOk();
        webTestClient
                .post()
                .uri("/links")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(TG_CHAT_ID, "1")
                .bodyValue(asJsonString(stackOverflowLink))
                .exchange()
                .expectStatus()
                .is4xxClientError();
        var links = webTestClient
                .get()
                .uri("/links")
                .header(TG_CHAT_ID, "1")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(ListLinkResponse.class)
                .returnResult()
                .getResponseBody()
                .links();

        assertThat(links).hasSize(1).map(LinkResponse::url).containsExactlyInAnyOrder(stackOverflowLink.getLink());
        assertThat(linkRepository.findAll())
                .hasSize(1)
                .allMatch(link ->
                        link.getChatIds().size() == 1 && link.getChatIds().contains(1L));
    }

    @Test
    void getLinkDiffIds() {
        webTestClient
                .post()
                .uri("/links")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(TG_CHAT_ID, "1")
                .bodyValue(asJsonString(stackOverflowLink))
                .exchange()
                .expectStatus()
                .isOk();
        webTestClient
                .post()
                .uri("/links")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(TG_CHAT_ID, "2")
                .bodyValue(asJsonString(stackOverflowLink))
                .exchange()
                .expectStatus()
                .isOk();

        assertThat(linkRepository.findAll()).singleElement().satisfies(link -> {
            assertThat(link.getChatIds()).hasSize(2).containsExactlyInAnyOrder(1L, 2L);
            assertThat(link.getUrl()).isEqualTo(stackOverflowLink.getLink());
        });
    }

    @Test
    void removeLink() {
        webTestClient
                .post()
                .uri("/links")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(TG_CHAT_ID, "1")
                .bodyValue(asJsonString(githubLink))
                .exchange()
                .expectStatus()
                .isOk();

        webTestClient
                .method(HttpMethod.DELETE)
                .uri("/links")
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(TG_CHAT_ID, "1")
                .bodyValue(new RemoveLinkRequest(githubLink.getLink()))
                .exchange()
                .expectStatus()
                .isOk();

        assertThat(linkRepository.findAll()).isEmpty();
    }
}
