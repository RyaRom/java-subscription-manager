package backend.academy.scrapper.clients;

import backend.academy.dto.LinkUpdate;
import backend.academy.scrapper.repository.dto.GithubResponseDto.Activity;
import backend.academy.scrapper.repository.dto.Link;
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

    public static String getGithubUpdate(Activity activity) {
        return String.format("type: %s; timestamp: %s; author: %s",
            activity.activityType().toString(),
            activity.timestamp().toString(),
            activity.actor().login());
    }

    public Mono<Void> sendUpdate(Activity activity, Link link) {
        LinkUpdate linkUpdate = LinkUpdate.builder()
            .linkId(link.linkId())
            .url(link.url())
            .tgChatIds(link.chatIds())
            .description(getGithubUpdate(activity))
            .build();
        return webClient.post()
            .uri("/updates")
            .body(BodyInserters.fromValue(linkUpdate))
            .retrieve()
            .toBodilessEntity()
            .then();
    }
}
