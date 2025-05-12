package integration.api;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinkResponse;
import backend.academy.dto.RemoveLinkRequest;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.dto.LinkType;
import backend.academy.scrapper.repository.entities.GithubInfoEntity;
import backend.academy.scrapper.repository.entities.StackOverflowInfoEntity;
import integration.BaseIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import static backend.academy.configuration.CustomHeaders.TG_CHAT_ID;
import static org.assertj.core.api.Assertions.assertThat;

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
            assertThat(link.getLinkType()).isEqualTo(LinkType.GITHUB);
            assertThat(link.getLinkInfo()).isNotNull();
            assertThat(link.getLinkInfo()).isInstanceOf(GithubInfoEntity.class);
            var githubInfo = (GithubInfoEntity) link.getLinkInfo();
            assertThat(githubInfo.getOwner()).isEqualTo("RyaRom");
            assertThat(githubInfo.getRepo()).isEqualTo("HackChangeHackathon2024");
            assertThat(link.getChatIdList()).contains(1L);
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
                    "https://stackoverflow.com/questions/1732348/regex-match-open-tags-except-xhtml-self-contained" +
                        "-tags");
            assertThat(link.getLinkType()).isEqualTo(LinkType.STACK_OVERFLOW);
            assertThat(link.getLinkInfo()).isNotNull();
            assertThat(link.getLinkInfo()).isInstanceOf(StackOverflowInfoEntity.class);
            var stackOverflowInfo = (StackOverflowInfoEntity) link.getLinkInfo();
            assertThat(stackOverflowInfo.getQuestionId()).isEqualTo(1732348);
            assertThat(link.getChatIdList()).contains(1L);
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
                link.getChatIdList().size() == 1 && link.getChatIdList().contains(1L));
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
                link.getChatIdList().size() == 1 && link.getChatIdList().contains(1L));
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
            assertThat(link.getChatIdList()).hasSize(2).containsExactlyInAnyOrder(1L, 2L);
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
