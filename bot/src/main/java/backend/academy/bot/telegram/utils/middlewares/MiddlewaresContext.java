package backend.academy.bot.telegram.utils.middlewares;

import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
@Log4j2
public class MiddlewaresContext<REQUEST, RESPONSE> {
    private final List<AbstractMiddleware<REQUEST, RESPONSE>> middlewares;

    public Mono<RESPONSE> applyMiddlewares(
        Mono<REQUEST> request,
        Function<Mono<REQUEST>, Mono<RESPONSE>> process) {
        for (var middleware : middlewares) {
            request = middleware.preHandle(request)
                .map(it -> {
                    log.info("Before request {}. In middleware {}", it, middleware.getClass());
                    return it;
                });
        }

        var response = process.apply(request);

        for (var middleware : middlewares) {
            response = middleware.postHandle(response)
                .then(Mono.fromRunnable(() -> log.info("In middleware {}", middleware.getClass())));
        }

        return response;
    }
}
