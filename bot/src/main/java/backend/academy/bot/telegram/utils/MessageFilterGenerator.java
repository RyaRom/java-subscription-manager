package backend.academy.bot.telegram.utils;

import com.pengrad.telegrambot.model.Message;
import java.util.Map;
import java.util.function.Predicate;

@FunctionalInterface
public interface MessageFilterGenerator {
    Predicate<Message> filter(Map<String, Object> kwargs);
}
