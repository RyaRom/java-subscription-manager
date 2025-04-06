package backend.academy.bot.telegram.sdk.middlewares;

import com.pengrad.telegrambot.model.Update;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
@Log4j2
public class MiddlewaresContext {
    private final List<AbstractMiddleware> middlewares;

    public Mono<Update> applyMiddlewares(Mono<Update> request, Mono<Update> process) {
        var pipeline = request;
        for (var middleware : middlewares) {
            pipeline = middleware
                    .preHandle(pipeline)
                    .doOnSuccess(it -> log.info("In middleware {}; Before request {}", it, middleware.getClass()));
        }

        pipeline = pipeline.then(process);

        for (var middleware : middlewares) {
            pipeline = middleware
                    .postHandle(pipeline)
                    .doOnSuccess(it -> log.info("In middleware {}. After request {}.", it, middleware.getClass()));
        }

        return pipeline;
    }
}
