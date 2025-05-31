package backend.academy.bot.rest;

import backend.academy.resilience.RateLimit;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Log4j2
@Component
public class SimpleComponent {
    @RateLimit(4)
    public Mono<String> doStuff() {
        return Mono.just("Hello, world!")
            .doOnEach(it ->log.info("Got {}", it.get()));
    }
}
