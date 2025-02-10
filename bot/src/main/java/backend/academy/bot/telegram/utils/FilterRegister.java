package backend.academy.bot.telegram.utils;

import com.pengrad.telegrambot.model.Message;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class FilterRegister {
    private static final Map<Class<? extends MessageFilterGenerator>, MessageFilterGenerator> FILTER_CACHE = new HashMap<>();

    public static MessageFilterGenerator getFilterInstance(Class<? extends MessageFilterGenerator> filterClass) {
        return FILTER_CACHE.computeIfAbsent(filterClass, aClass -> {
            try {
                return aClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Unable to create filter instance", e);
            }
        });
    }

    public static class CommandFilter implements MessageFilterGenerator {

        @Override
        public Predicate<Message> filter(Map<String, Object> kwargs) {
            Set<String> commandsSet = new HashSet<>();
            Object command = kwargs.get("commands");
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
                return text != null
                    && !text.isBlank()
                    && commandsSet.contains(text);
            };
        }
    }
}
