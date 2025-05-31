package backend.academy.scrapper.clients;

import backend.academy.dto.LinkUpdate;
import io.github.resilience4j.retry.annotation.Retry;
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
public class BotHttpClient implements BotClient {
    private final WebClient botWebClient;

    @Override
    @Retry(name = "base")
    public Mono<Void> sendUpdate(LinkUpdate linkUpdate) {
        return botWebClient
                .post()
                .uri("/updates")
                .body(BodyInserters.fromValue(linkUpdate))
                .retrieve()
                .toBodilessEntity()
                .then();
    }
}
