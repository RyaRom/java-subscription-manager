package backend.academy.scrapper.resilience;

import backend.academy.scrapper.config.ClientsProps;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@RequiredArgsConstructor
@Service
@Log4j2
public class RetryService {
    private final ClientsProps clientsProps;

    public <T> Mono<T> withRetry(Mono<T> request) {
        return request.retryWhen(Retry.backoff(
                        clientsProps.retry().maxAttempts(),
                        Duration.ofMillis(clientsProps.retry().waitDuration()))
                .filter(RetryService::retryWhen));
    }

    public <T> Flux<T> withRetry(Flux<T> request) {
        return request.retryWhen(Retry.backoff(
                        clientsProps.retry().maxAttempts(),
                        Duration.ofMillis(clientsProps.retry().waitDuration()))
                .filter(RetryService::retryWhen));
    }

    private static boolean retryWhen(Throwable e) {
        log.info("Got error {} : {} in request", e.getMessage(), e);
        if (e instanceof WebClientResponseException responseException) {
            log.info("Error {} status {}", responseException.getMessage(), responseException.getStatusCode());
            return responseException.getStatusCode().is5xxServerError()
                    || responseException.getStatusCode().value() == 429;
        }
        return e instanceof WebClientRequestException;
    }
}
