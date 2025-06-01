package backend.academy.scrapper.rest;

import backend.academy.resilience2.RateLimit;
import backend.academy.scrapper.service.UpdatePollingJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Log4j2
@RestController
@RequestMapping("/scrapper/api")
@RequiredArgsConstructor
public class AdminController {
    private final UpdatePollingJob updatePollingJob;

    @RateLimit
    @PostMapping("/test/update")
    public Mono<Integer> fetchUpdate() {
        return Mono.fromCallable(() -> {
            updatePollingJob.update();
            return 2;
        });
    }
}
