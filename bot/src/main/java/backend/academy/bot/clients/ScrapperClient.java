package backend.academy.bot.clients;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.ListLinkResponse;
import backend.academy.dto.RemoveLinkRequest;
import backend.academy.exception.BadLinkException;
import backend.academy.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import static backend.academy.configuration.CustomHeaders.TG_CHAT_ID;

@Component
@RequiredArgsConstructor
public class ScrapperClient {
    private final WebClient scrapperHttpClient;

    public Mono<Void> registerChat(Long chatId) {
        var result = scrapperHttpClient
            .post()
            .uri("/tg-chat/{chatId}", chatId)
            .retrieve();
        return handleErrorsDefault(result)
            .toBodilessEntity()
            .then()
            .publishOn(Schedulers.boundedElastic());
    }

    public Mono<ListLinkResponse> getLinks(Long chatId) {
        var result = scrapperHttpClient
            .get()
            .uri("/links")
            .header(TG_CHAT_ID, chatId.toString())
            .retrieve();
        return handleErrorsDefault(result)
            .bodyToMono(ListLinkResponse.class)
            .publishOn(Schedulers.boundedElastic());
    }

    public Mono<Void> addLink(Long chatId, AddLinkRequest addLinkRequest) {
        var result = scrapperHttpClient
            .post()
            .uri("/links")
            .header(TG_CHAT_ID, chatId.toString())
            .body(BodyInserters.fromValue(addLinkRequest))
            .retrieve();
        return handleErrorsDefault(result)
            .onStatus(code -> code.equals(HttpStatusCode.valueOf(400)), response ->
                response.bodyToMono(String.class)
                    .flatMap(body -> Mono.error(new BadLinkException("Bad request: " + body)))
            )
            .toBodilessEntity()
            .then()
            .publishOn(Schedulers.boundedElastic());
    }

    public Mono<Void> removeLink(Long chatId, String link) {
        var result = scrapperHttpClient
            // body in delete is not allowed by default
            .method(HttpMethod.DELETE)
            .uri("/links")
            .header(TG_CHAT_ID, chatId.toString())
            .body(BodyInserters.fromValue(new RemoveLinkRequest(link)))
            .retrieve();
        return handleErrorsDefault(result)
            .toBodilessEntity()
            .then()
            .publishOn(Schedulers.boundedElastic());
    }

    private WebClient.ResponseSpec handleErrorsDefault(WebClient.ResponseSpec responseSpec){
        return responseSpec.onStatus(code -> code.equals(HttpStatusCode.valueOf(404)), response ->
                response.bodyToMono(String.class)
                    .flatMap(body -> Mono.error(new ResourceNotFoundException("Not found resource")))
            )
            .onStatus(HttpStatusCode::is5xxServerError, response ->
                response.bodyToMono(String.class)
                    .flatMap(body -> Mono.error(new RuntimeException("Server error: " + body)))
            );
    }
}
