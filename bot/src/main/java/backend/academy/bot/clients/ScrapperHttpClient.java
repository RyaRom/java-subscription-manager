package backend.academy.bot.clients;

import backend.academy.bot.config.ClientsProps;
import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.ApiErrorResponse;
import backend.academy.dto.ListLinkResponse;
import backend.academy.dto.RemoveLinkRequest;
import backend.academy.exception.BadLinkException;
import backend.academy.exception.LinkDuplicatedException;
import backend.academy.exception.ResourceNotFoundException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;
import static backend.academy.configuration.CustomHeaders.TG_CHAT_ID;

@RequiredArgsConstructor
@Log4j2
@Component
@Profile({"dev"})
public class ScrapperHttpClient implements ScrapperPublisher, ScrapperClient {
    private final WebClient scrapperWebClient;
    private final ClientsProps clientsProps;

    @Override
    public Mono<Void> registerChat(Long chatId) {
        var result = scrapperWebClient.post().uri("/tg-chat/{chatId}", chatId).retrieve();
        return withRetry(handleErrorsDefault(result).toBodilessEntity())
            .then().publishOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<ListLinkResponse> getLinks(Long chatId) {
        var result = scrapperWebClient
            .get()
            .uri("/links")
            .header(TG_CHAT_ID, chatId.toString())
            .retrieve();
        return withRetry(handleErrorsDefault(result).bodyToMono(ListLinkResponse.class))
            .publishOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> addLink(Long chatId, AddLinkRequest addLinkRequest) {
        var result = scrapperWebClient
            .post()
            .uri("/links")
            .header(TG_CHAT_ID, chatId.toString())
            .body(BodyInserters.fromValue(addLinkRequest))
            .retrieve();
        return withRetry(handleErrorsDefault(result).toBodilessEntity())
            .then().publishOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> removeLink(Long chatId, String link) {
        var result = scrapperWebClient
            // body in delete is not allowed by default
            .method(HttpMethod.DELETE)
            .uri("/links")
            .header(TG_CHAT_ID, chatId.toString())
            .body(BodyInserters.fromValue(new RemoveLinkRequest(link)))
            .retrieve();
        return withRetry(handleErrorsDefault(result).toBodilessEntity())
            .then().publishOn(Schedulers.boundedElastic());
    }

    private WebClient.ResponseSpec handleErrorsDefault(WebClient.ResponseSpec responseSpec) {
        return responseSpec
            .onStatus(code -> code.equals(HttpStatusCode.valueOf(404)), response -> response.bodyToMono(
                    ApiErrorResponse.class)
                .flatMap(body -> Mono.error(new ResourceNotFoundException("Not found resource"))))
            .onStatus(HttpStatusCode::is5xxServerError, response -> response.bodyToMono(String.class)
                .flatMap(body -> Mono.error(new RuntimeException("Server error: " + body))))
            .onStatus(code -> code.equals(HttpStatusCode.valueOf(400)), response -> response.bodyToMono(
                    ApiErrorResponse.class)
                .flatMap(ScrapperHttpClient::map400Error));
    }

    private <T> Mono<T> withRetry(Mono<T> request) {
        return request.retryWhen(Retry.backoff(clientsProps.retry().maxAttempts(),
                Duration.ofMillis(clientsProps.retry().waitDuration()))
            .filter(e -> {
                log.info("Got error {} : {} in request", e.getMessage(), e);
                if (e instanceof WebClientResponseException responseException) {
                    log.info("Error {} status {}",
                        responseException.getMessage(),
                        responseException.getStatusCode());
                    return responseException.getStatusCode().is5xxServerError()
                        || responseException.getStatusCode().value() == 429;
                }
                return e instanceof WebClientRequestException;
            }));
    }

    private static @NotNull Mono<Throwable> map400Error(ApiErrorResponse body) {
        log.warn("Got error {}", body);
        Class<?> exception;
        try {
            exception = Class.forName(body.exceptionName());
        } catch (ClassNotFoundException e) {
            log.warn("Unexpected error type: {}", body.exceptionName());
            return Mono.error(new RuntimeException("Unexpected error type: " + body.exceptionName()));
        }
        if (exception.equals(BadLinkException.class)) {
            return Mono.error(new BadLinkException("Bad link: " + body));
        }
        if (exception.equals(LinkDuplicatedException.class)) {
            return Mono.error(new LinkDuplicatedException("Link duplicated: " + body));
        }
        return Mono.error(new RuntimeException("Unexpected error type: " + body.exceptionName()));
    }
}
