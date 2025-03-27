package backend.academy.bot.clients;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.ListLinkResponse;
import backend.academy.dto.RemoveLinkRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
public class ScrapperClient {
    public static final String TG_CHAT_ID = "Tg-Chat-Id";
    private final WebClient scrapperHttpClient;

    public Mono<Void> registerChat(Long chatId) {
        return scrapperHttpClient
            .post()
            .uri("/tg-chat/{chatId}", chatId)
            .retrieve()
            .toBodilessEntity()
            .then()
            .publishOn(Schedulers.boundedElastic());
    }

    public Mono<ListLinkResponse> getLinks(Long chatId) {
        return scrapperHttpClient
            .get()
            .uri("/links")
            .header(TG_CHAT_ID, chatId.toString())
            .retrieve()
            .bodyToMono(ListLinkResponse.class)
            .publishOn(Schedulers.boundedElastic());
    }

    public Mono<Void> addLink(Long chatId, AddLinkRequest addLinkRequest) {
        return scrapperHttpClient
            .post()
            .uri("/links")
            .header(TG_CHAT_ID, chatId.toString())
            .body(BodyInserters.fromValue(addLinkRequest))
            .retrieve()
            .toBodilessEntity()
            .then()
            .publishOn(Schedulers.boundedElastic());
    }

    public Mono<Void> removeLink(Long chatId, String link) {
        return scrapperHttpClient
            // body in delete is not allowed by default
            .method(HttpMethod.DELETE)
            .uri("/links")
            .header(TG_CHAT_ID, chatId.toString())
            .body(BodyInserters.fromValue(new RemoveLinkRequest(link)))
            .retrieve()
            .toBodilessEntity()
            .then()
            .publishOn(Schedulers.boundedElastic());
    }
}
