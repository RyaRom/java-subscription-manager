package backend.academy.bot.telegram.utils.filters;

import backend.academy.bot.telegram.utils.fsm.FSMContext;
import com.pengrad.telegrambot.model.Message;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@Log4j2
@RequiredArgsConstructor
public class FilterRegister {
    private static final Map<Class<? extends MessageFilterGenerator>, MessageFilterGenerator> FILTER_CACHE = new HashMap<>();
    private final ApplicationContext applicationContext;

    private static boolean notEmpty(String text) {
        return text != null && !text.isBlank();
    }

    public MessageFilterGenerator getFilterInstance(Class<? extends MessageFilterGenerator> filterClass) {
        return FILTER_CACHE.computeIfAbsent(filterClass, aClass -> {
            try {
                try {
                    return applicationContext.getBean(filterClass);
                } catch (Exception e) {
                    return aClass.getDeclaredConstructor().newInstance();
                }
            } catch (Exception e) {
                log.error("Unable to create filter instance {}", filterClass);
                throw new RuntimeException("Unable to create filter instance", e);
            }
        });
    }

    public static class CommandFilter implements MessageFilterGenerator {

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
                return notEmpty(text)
                    && commandsSet.contains(text);
            };
        }
    }

    @RequiredArgsConstructor
    @Component
    public static class StateFilter implements MessageFilterGenerator {
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
                return notEmpty(text)
                    && states.contains(stateName);
            };
        }
    }

    public static class UrlFilter implements MessageFilterGenerator {

        @Override
        public Predicate<Message> filter(Map<FilterParameter, Object> kwargs) {
            return message -> {
                String text = message.text();
                return notEmpty(text)
                    && text.matches(
                    "(https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]" +
                        "\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|" +
                        "https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9]+\\.[^\\s]{2,}|www\\.[a-zA-" +
                        "Z0-9]+\\.[^\\s]{2,})"
                );
            };
        }
    }

    public static class NotEmptyTextFilter implements MessageFilterGenerator {

        @Override
        public Predicate<Message> filter(Map<FilterParameter, Object> kwargs) {
            return message -> notEmpty(message.text());
        }
    }
}
