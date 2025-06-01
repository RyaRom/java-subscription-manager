package backend.academy.resilience2.impl;

import backend.academy.configuration.ResilienceProps;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Log4j2
@RequiredArgsConstructor
public class ReactorRetrier {
    private final ResilienceProps.Retry retryProps;

    public <T> Mono<T> withRetry(Mono<T> mono) {
        return mono.retryWhen(Retry.fixedDelay(
                    retryProps.maxAttempts(),
                    Duration.ofMillis(retryProps.waitDuration())
                ).filter(this::willBeRetried)
                .doAfterRetry(signal ->
                    log.info("Retrying for {} error {}",
                        signal.totalRetries(), signal.failure())))
            .onErrorMap(e -> {
                log.error("Retry failed", e);
                return e.getCause();
            });
    }

    public <T> Flux<T> withRetry(Flux<T> flux) {
        return flux.retryWhen(Retry.fixedDelay(
                    retryProps.maxAttempts(),
                    Duration.ofMillis(retryProps.waitDuration())
                ).filter(this::willBeRetried)
                .doAfterRetry(signal ->
                    log.info("Retrying for {} error {}",
                        signal.totalRetries(), signal.failure())))
            .onErrorMap(e -> {
                log.error("Retries exhausted", e);
                return e.getCause();
            });
    }

    private boolean willBeRetried(Throwable e) {
        if (e instanceof WebClientResponseException responseException) {
            boolean toRetry = !retryProps.blacklistedStatusCodes().contains(
                responseException.getStatusCode().value());
            log.info("Will be retried? : {}", toRetry);
            return toRetry;
        }
//                return !retryProps.blacklistedExceptions().contains(e.getClass().getName());
        return true;
    }
}
