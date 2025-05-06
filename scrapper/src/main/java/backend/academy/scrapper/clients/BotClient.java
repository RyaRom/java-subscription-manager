package backend.academy.scrapper.clients;

import backend.academy.dto.LinkUpdate;
import backend.academy.scrapper.repository.dto.GithubActivity;
import backend.academy.scrapper.repository.dto.StackAnswersResponseDto;
import backend.academy.scrapper.repository.entities.LinkEntity;
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

    public static String getGithubUpdate(GithubActivity githubActivity) {
        return String.format(
            "Update in github repo:\n type: %s; timestamp: %s; author: %s",
            githubActivity.activityType().toString(),
            githubActivity.timestamp().toString(),
            githubActivity.actor().login());
    }

    public static String getStackAnswerUpdate(StackAnswersResponseDto stackAnswersResponseDto) {
        return String.format("""
                New Stack overflow update in %s:
                Question: %s
                User: %s
                Text: %s""",
            stackAnswersResponseDto.getLink());
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

    public Mono<Void> sendUpdate(GithubActivity githubActivity, LinkEntity link) {
        if (githubActivity.activityType() == GithubActivity.ActivityType.UNKNOWN) {
            log.warn("Unknown type in update {}", githubActivity);
        }
        LinkUpdate linkUpdate = LinkUpdate.builder()
            .linkId(link.getLinkId())
            .url(link.getUrl())
            .tgChatIds(link.getChatIdList())
            .description(getGithubUpdate(githubActivity))
            .build();
        return sendUpdate(linkUpdate);
    }

    public Mono<Void> sendUpdate(StackAnswersResponseDto answer, LinkEntity link) {
        LinkUpdate linkUpdate = LinkUpdate.builder()
            .linkId(link.getLinkId())
            .url(link.getUrl())
            .tgChatIds(link.getChatIdList())
            .description(getStackAnswerUpdate(answer))
            .build();
        return sendUpdate(linkUpdate);
    }
}
