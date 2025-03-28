package backend.academy.bot.telegram.utils.middlewares;

import com.pengrad.telegrambot.model.Update;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Simple example for testing
 */
@Component
@Log4j2
public class LoggingMiddleware implements AbstractMiddleware<Update, Void>{
    @Override
    public Mono<Update> preHandle(Mono<Update> chain) {
        return chain.map(update -> {
            log.info("Before update in chat {}", update.message().chat().id());
            return update;
        });
    }

    @Override
    public Mono<Void> postHandle(Mono<Void> chain) {
        return chain.then(Mono.fromRunnable(() -> log.info("After update in chat")));
    }
}
