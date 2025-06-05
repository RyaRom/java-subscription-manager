package backend.academy.resilience2.impl;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CircuitBreaker {
    boolean process();

    Mono<?> addOnErrorCallback(Mono<?> chain);

    Flux<?> addOnErrorCallback(Flux<?> chain);

    enum State {
        CLOSED,
        OPEN,
        HALF_OPEN
    }
}
