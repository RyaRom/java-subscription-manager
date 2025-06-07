package integration.testcontainers.resilience;

import backend.academy.resilience2.CircuitBreaker;
import backend.academy.resilience2.Fallback;
import backend.academy.resilience2.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
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
            if (true) {
                throw new RuntimeException("error");
            }
            return stuff;
        }).doOnError((e) -> System.err.println("ERORRORROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROROR"));
    }

    @Retry
    @CircuitBreaker
    @Fallback("fallbackMethod")
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
}
