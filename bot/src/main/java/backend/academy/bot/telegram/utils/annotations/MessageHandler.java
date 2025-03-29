package backend.academy.bot.telegram.utils.annotations;

import backend.academy.bot.telegram.utils.filters.AbstractFilter;
import backend.academy.bot.telegram.utils.filters.MessageFilter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for Telegram message handlers. Required method to have Method parameter of type
 * {@link com.pengrad.telegrambot.model.Message}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MessageHandler {
    /**
     * List of filter classes to apply before calling the annotated method.
     */
    Class<? extends AbstractFilter>[] filters() default {};

    /**
     * List of filter parameters to use when calling the annotated method.
     */
    FilterParam[] params() default {};

    /**
     * Priority of the handler.
     */
    int priority() default 5;

    /**
     * If set to true, this handler will be the last one to be called.
     */
    boolean isFinal() default true;
}
