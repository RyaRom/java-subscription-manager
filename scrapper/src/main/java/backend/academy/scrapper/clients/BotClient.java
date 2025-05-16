package backend.academy.scrapper.clients;

import backend.academy.dto.LinkUpdate;
import backend.academy.scrapper.repository.links.dto.github.GithubFullInfo;
import backend.academy.scrapper.repository.links.dto.stackOverflow.StackOverflowFullInfo;
import backend.academy.scrapper.repository.links.entities.LinkEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Log4j2
@SuppressWarnings("VA_FORMAT_STRING_USES_NEWLINE")
public class BotClient {
    private final WebClient botHttpClient;

    public static String getGithubUpdate(GithubFullInfo info) {
        return String.format("""
                New Github update (%s): %s
                User: %s
                Text: %s""",
            info.type(),
            info.title(),
            info.username(),
            info.body()
        );
    }

    public static String getStackAnswerUpdate(StackOverflowFullInfo info) {
        return String.format("""
                New Stack overflow %s
                Question: %s
                User: %s
                Text: %s""",
            info.type(),
            info.questionTitle(),
            info.username(),
            info.body()
        );
    }

    public Mono<Void> sendUpdate(LinkUpdate linkUpdate) {
        return botHttpClient
            .post()
            .uri("/updates")
            .body(BodyInserters.fromValue(linkUpdate))
            .retrieve()
            .toBodilessEntity()
            .then();
    }

    public Mono<Void> sendUpdate(GithubFullInfo githubActivity, LinkEntity link) {
        if (githubActivity.type().isEmpty()) {
            log.warn("Unknown type in github update {}", githubActivity);
        }
        LinkUpdate linkUpdate = LinkUpdate.builder()
            .linkId(link.getLinkId())
            .url(link.getUrl())
            .tgChatIds(link.getChatIdList())
            .description(getGithubUpdate(githubActivity))
            .build();
        return sendUpdate(linkUpdate);
    }

    public Mono<Void> sendUpdate(StackOverflowFullInfo answer, LinkEntity link) {
        if (answer.type().isEmpty()) {
            log.warn("Unknown type in stack update {}", answer);
        }
        LinkUpdate linkUpdate = LinkUpdate.builder()
            .linkId(link.getLinkId())
            .url(link.getUrl())
            .tgChatIds(link.getChatIdList())
            .description(getStackAnswerUpdate(answer))
            .build();
        return sendUpdate(linkUpdate);
    }
}
