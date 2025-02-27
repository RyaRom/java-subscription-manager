package backend.academy.bot.clients;

import backend.academy.bot.rest.dto.AddLinkRequest;
import backend.academy.bot.rest.dto.ListLinkResponse;
import backend.academy.bot.rest.dto.RemoveLinkRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
public class ScrapperClient {
    @Qualifier("scrapperHttpClient")
    private final WebClient webClient;

    public Mono<Void> registerChat(Long chatId) {
        return webClient.post()
            .uri("/tg-chat/{chatId}", chatId)
            .retrieve()
            .toBodilessEntity()
            .then()
            .publishOn(Schedulers.boundedElastic());
    }

    public Mono<ListLinkResponse> getLinks(Long chatId) {
        return webClient.get()
            .uri("/links")
            .header("Tg-Chat-Id", chatId.toString())
            .retrieve()
            .bodyToMono(ListLinkResponse.class)
            .publishOn(Schedulers.boundedElastic());
    }

    public Mono<Void> addLink(Long chatId, AddLinkRequest addLinkRequest) {
        return webClient.post()
            .uri("/links")
            .header("Tg-Chat-Id", chatId.toString())
            .body(BodyInserters.fromValue(addLinkRequest))
            .retrieve()
            .toBodilessEntity()
            .then()
            .publishOn(Schedulers.boundedElastic());
    }

    public Mono<Void> removeLink(Long chatId, String link) {
        return webClient
            //body in delete is not allowed by default
            .method(HttpMethod.DELETE)
            .uri("/links")
            .header("Tg-Chat-Id", chatId.toString())
            .body(BodyInserters.fromValue(new RemoveLinkRequest(link)))
            .retrieve()
            .toBodilessEntity()
            .then()
            .publishOn(Schedulers.boundedElastic());
    }
}
