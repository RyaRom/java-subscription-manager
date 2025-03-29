package backend.academy.bot.telegram.utils.filters;

import com.pengrad.telegrambot.model.Message;
import java.util.Map;
import java.util.function.Predicate;

@FunctionalInterface
public interface MessageFilter extends AbstractFilter<Message>{
}
