package backend.academy.bot.telegram.utils.middlewares;

import reactor.core.publisher.Mono;

public interface AbstractMiddleware<REQUEST, RESPONSE> {
    Mono<REQUEST> preHandle(Mono<REQUEST> chain);

    Mono<RESPONSE> postHandle(Mono<RESPONSE> chain);
}
