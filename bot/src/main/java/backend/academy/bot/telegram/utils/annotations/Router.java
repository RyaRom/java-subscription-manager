package backend.academy.bot.telegram.utils.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.stereotype.Component;

/**
 * Marks a class as a handler for telegram updates.
 * <p>
 * Each public method of a handler class should be annotated with {@link MessageHandler}
 * or other update handler annotations
 * and return {@link reactor.core.publisher.Mono} or void.
 */
@Component
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Router {
}
