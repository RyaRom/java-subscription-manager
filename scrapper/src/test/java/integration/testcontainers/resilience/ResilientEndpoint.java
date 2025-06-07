package integration.testcontainers.resilience;

import backend.academy.resilience2.RateLimit;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("testing")
@RestController
public class ResilientEndpoint {
    @RateLimit
    @GetMapping("/mono")
    public Mono<String> getMono() {
        return Mono.fromCallable(() -> {
            innerLogic();
            return "Hello, world";
        });
    }

    @RateLimit
    @GetMapping("/flux")
    public Flux<String> getFlux() {
        return Flux.from(Mono.fromCallable(() -> {
            innerLogic();
            return "Hello, world";
        }));
    }

    public void innerLogic() {
        System.err.println("INSIDE ENDPOINT");
    }
}
