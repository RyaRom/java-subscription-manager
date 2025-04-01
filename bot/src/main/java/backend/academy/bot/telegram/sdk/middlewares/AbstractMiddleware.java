package backend.academy.bot.telegram.sdk.middlewares;

import com.pengrad.telegrambot.model.Update;
import reactor.core.publisher.Mono;

public interface AbstractMiddleware {
    Mono<Update> preHandle(Mono<Update> chain);

    Mono<Update> postHandle(Mono<Update> chain);
}
