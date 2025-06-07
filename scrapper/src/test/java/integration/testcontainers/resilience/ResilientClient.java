package integration.testcontainers.resilience;

import backend.academy.resilience2.CircuitBreaker;
import backend.academy.resilience2.Fallback;
import backend.academy.resilience2.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Profile("testing")
@RequiredArgsConstructor
public class ResilientClient {
    @Retry
    @CircuitBreaker
    @Fallback("fallbackMethod")
    public Mono<String> doMonoFull(String stuff) {
        return Mono.fromCallable(() -> {
            innerLogic();
            return stuff;
        });
    }

    @Retry
    public Mono<String> doMonoRetry(String stuff) {
        return Mono.fromCallable(() -> {
            innerLogic();
            if (true) {
                throw new RuntimeException("some error");
            }
            return stuff;
        });
    }

    @CircuitBreaker
    public Mono<String> doMonoCircuitBreaker(String stuff) {
        return Mono.fromCallable(() -> {
            innerLogic();
            if (true) {
                throw new RuntimeException("circuit breaker error");
            }
            return stuff;
        });
    }

    @CircuitBreaker
    public Mono<String> doMonoCircuitBreakerConditional(boolean isError) {
        return Mono.fromCallable(() -> {
            if (isError) {
                throw new RuntimeException("circuit breaker error");
            }
            innerLogic();
            return "hiii:3";
        });
    }

    @Fallback("fallbackMethod")
    public Mono<String> doMonoFallback(String stuff) {
        return Mono.fromCallable(() -> {
            innerLogic();
            if (true) {
                throw new RuntimeException("fallback error");
            }
            return stuff;
        });
    }

    @Retry
    @CircuitBreaker
    public Mono<String> doMonoRetryWithBreaker(String stuff) {
        return Mono.fromCallable(() -> {
            if (true) {
                throw new RuntimeException("retry with breaker error");
            }
            innerLogic();
            return stuff;
        });
    }

    @Retry
    public Mono<String> doMonoNotRetry(String stuff) {
        return Mono.fromCallable(() -> {
            innerLogic();
            if (true) {
                throw get400Error();
            }
            return stuff;
        });    }

    @Fallback("fallbackMethod")
    @Retry
    public Mono<String> doMonoFallbackWithRetry(String stuff) {
        return Mono.fromCallable(() -> {
            innerLogic();
            if (true) {
                throw new RuntimeException("fallback with retry error");
            }
            return stuff;
        });
    }

    @Fallback("fallbackMethod")
    @CircuitBreaker
    public Mono<String> doMonoFallbackWithBreaker(String stuff) {
        return Mono.fromCallable(() -> {
            innerLogic();
            if (true) {
                throw new RuntimeException("fallback with breaker error");
            }
            return stuff;
        });
    }

    @Retry
    @CircuitBreaker
    @Fallback("fallbackFluxMethod")
    public Flux<String> doFluxFull(String stuff) {
        return Flux.from(Mono.fromCallable(() -> {
            innerLogic();
            return stuff;
        }));
    }

    public void innerLogic() {
        System.err.println("INSIDE CLIENT");
    }

    public Mono<String> fallbackMethod(String stuff) {
        return Mono.fromCallable(() -> {
            System.err.println("INSIDE FALLBACK");
            return stuff + "_afterError";
        });
    }

    public Flux<String> fallbackFluxMethod(String stuff) {
        return Flux.just(stuff + "_afterError");
    }

    private static WebClientResponseException get400Error() {
        return new WebClientResponseException(
                400,
                "Too Many Requests",
                HttpHeaders.EMPTY,
                null,
                null);
    }
}
