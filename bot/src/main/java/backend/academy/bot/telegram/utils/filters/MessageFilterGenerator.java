package backend.academy.bot.telegram.utils.filters;

import com.pengrad.telegrambot.model.Message;
import java.util.Map;
import java.util.function.Predicate;

@FunctionalInterface
public interface MessageFilterGenerator {
    Predicate<Message> filter(Map<FilterParameter, Object> kwargs);
}
