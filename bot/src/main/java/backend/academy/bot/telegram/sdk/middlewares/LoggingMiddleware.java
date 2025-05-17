package backend.academy.bot.telegram.sdk.middlewares;

import static backend.academy.bot.telegram.sdk.logging.MDCLogger.logOnNext;

import com.pengrad.telegrambot.model.Update;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/** Simple example for testing */
@Component
@Log4j2
public class LoggingMiddleware implements AbstractMiddleware {
    @Override
    public Mono<Update> preHandle(Mono<Update> chain) {
        return chain.doOnEach(logOnNext(update ->
                log.info("Before update in chat {}", update.message().chat().id())));
    }

    @Override
    public Mono<Update> postHandle(Mono<Update> chain) {
        return chain.doOnEach(logOnNext(update ->
                log.info("After update in chat {}", update.message().chat().id())));
    }
}
