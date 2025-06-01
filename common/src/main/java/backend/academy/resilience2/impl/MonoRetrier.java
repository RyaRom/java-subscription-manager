package backend.academy.resilience2.impl;

import backend.academy.configuration.ResilienceProps;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Log4j2
@RequiredArgsConstructor
public class MonoRetrier {
    private final ResilienceProps.Retry retryProps;

    public <T> Mono<T> withRetry(Mono<T> mono) {
        return mono.retryWhen(Retry.fixedDelay(
                retryProps.maxAttempts(),
                Duration.ofMillis(retryProps.waitDuration())
            ).filter(e -> {
                if (e instanceof WebClientResponseException responseException) {
                    return !retryProps.blacklistedStatusCodes().contains(
                        responseException.getStatusCode().value()
                    );
                }
                return true;
//                return !retryProps.blacklistedExceptions().contains(e.getClass().getName());
            })
            .doAfterRetry(signal ->
                log.info("Retrying for {} error {}",
                    signal.totalRetries(), signal.failure())))
            .onErrorMap(e -> {
                log.error("Retry failed", e);
                return e.getCause();
            });
    }
}
