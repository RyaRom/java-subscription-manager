package backend.academy.bot.telegram.sdk.filters;

import backend.academy.bot.telegram.sdk.annotations.Filter;
import backend.academy.bot.telegram.sdk.fsm.FSMContext;
import com.pengrad.telegrambot.model.Message;
import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@Log4j2
@RequiredArgsConstructor
public class FilterRegister {
    private final List<MessageFilter> filters;
    private final Map<Class<? extends MessageFilter>, MessageFilter> filterMap = new HashMap<>();

    private static boolean notEmpty(String text) {
        return text != null && !text.isBlank();
    }

    public MessageFilter getMessageFilterInstance(Class<? extends AbstractFilter> filterClass) {
        var instance = filterMap.get(filterClass);
        if (instance == null) {
            throw new RuntimeException("Filter " + filterClass.getSimpleName() + " not found. "
                    + "All filters should implement AbstractFilter interface");
        }
        return instance;
    }

    @PostConstruct
    private void init() {
        filters.forEach(filter -> filterMap.put(filter.getClass(), filter));
    }

    @Filter
    public static class CommandFilter implements MessageFilter {

        @Override
        public Predicate<Message> filter(Map<FilterParameter, Object> kwargs) {
            Set<String> commandsSet = new HashSet<>();
            Object command = kwargs.get(FilterParameter.COMMANDS);
            if (command instanceof String[] commands) {
                for (String cmd : commands) {
                    if (cmd.startsWith("/")) {
                        commandsSet.add(cmd);
                    } else {
                        commandsSet.add("/" + cmd);
                    }
                }
            }
            return message -> {
                String text = message.text();
                return notEmpty(text) && commandsSet.contains(text);
            };
        }
    }

    @Filter
    @RequiredArgsConstructor
    public static class StateFilter implements MessageFilter {
        private final FSMContext fsmContext;

        @Override
        public Predicate<Message> filter(Map<FilterParameter, Object> kwargs) {
            Set<String> states = new HashSet<>();
            Object state = kwargs.get(FilterParameter.STATE);
            if (state instanceof String[] stateArr) {
                Collections.addAll(states, stateArr);
            }

            return message -> {
                String text = message.text();
                String stateName = fsmContext.getCurrentStateName(message.chat().id());
                return notEmpty(text) && states.contains(stateName);
            };
        }
    }

    @Filter
    public static class UrlFilter implements MessageFilter {

        @Override
        public Predicate<Message> filter(Map<FilterParameter, Object> kwargs) {
            return message -> {
                String text = message.text();
                return notEmpty(text)
                        && text.matches("(https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]"
                                + "\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|"
                                + "https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9]+\\.[^\\s]{2,}|www\\.[a-zA-"
                                + "Z0-9]+\\.[^\\s]{2,})");
            };
        }
    }

    @Filter
    public static class NotEmptyTextFilter implements MessageFilter {

        @Override
        public Predicate<Message> filter(Map<FilterParameter, Object> kwargs) {
            return message -> notEmpty(message.text());
        }
    }
}
