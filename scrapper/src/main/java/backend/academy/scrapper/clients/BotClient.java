package backend.academy.scrapper.clients;

import backend.academy.dto.LinkUpdate;
import backend.academy.scrapper.repository.dto.GithubActivity;
import backend.academy.scrapper.repository.dto.Link;
import backend.academy.scrapper.repository.dto.StackAnswersResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class BotClient {
    @Qualifier("botHttpClient")
    private final WebClient webClient;

    public static String getGithubUpdate(GithubActivity githubActivity) {
        return String.format(
            "Update in github repo:\n type: %s; timestamp: %s; author: %s",
            githubActivity.activityType().toString(),
            githubActivity.timestamp().toString(),
            githubActivity.actor().login());
    }

    public static String getStackAnswerUpdate(StackAnswersResponseDto stackAnswersResponseDto) {
        return String.format(
            "New answer in so question:\n%s",
            stackAnswersResponseDto.getLink()
        );
    }

    public Mono<Void> sendUpdate(LinkUpdate linkUpdate) {
        return webClient
            .post()
            .uri("/updates")
            .body(BodyInserters.fromValue(linkUpdate))
            .retrieve()
            .toBodilessEntity()
            .then();
    }

    public Mono<Void> sendUpdate(GithubActivity githubActivity, Link link) {
        LinkUpdate linkUpdate = LinkUpdate.builder()
            .linkId(link.getLinkId())
            .url(link.getUrl())
            .tgChatIds(link.getChatIds().stream().toList())
            .description(getGithubUpdate(githubActivity))
            .build();
        return sendUpdate(linkUpdate);
    }

    public Mono<Void> sendUpdate(StackAnswersResponseDto answer, Link link) {
        LinkUpdate linkUpdate = LinkUpdate.builder()
            .linkId(link.getLinkId())
            .url(link.getUrl())
            .tgChatIds(link.getChatIds().stream().toList())
            .description(getStackAnswerUpdate(answer))
            .build();
        return sendUpdate(linkUpdate);
    }
}
