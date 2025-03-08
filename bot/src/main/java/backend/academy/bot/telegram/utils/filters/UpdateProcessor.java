package backend.academy.bot.telegram.utils.filters;

import backend.academy.bot.telegram.utils.annotations.FilterParam;
import backend.academy.bot.telegram.utils.annotations.Router;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import jakarta.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Log4j2
public class UpdateProcessor {
    private final FilterRegister filterRegister;
    private final ApplicationContext applicationContext;
    private final List<MessageHandler> messageHandlers = new ArrayList<>();

    private static Mono<Void> handleAsyncOrNotHandler(Object bean, Method method, Message message)
            throws IllegalAccessException, InvocationTargetException {
        if (method.getReturnType().equals(Void.TYPE)) {
            log.warn("Blocking call in {}", method.getName());
            return Mono.fromRunnable(() -> {
                        try {
                            method.invoke(bean, message);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .subscribeOn(Schedulers.boundedElastic())
                    .then();
        }
        Object result = method.invoke(bean, message);
        if (result instanceof Mono) {
            return (Mono<Void>) result;
        } else {
            log.error("Method {} must return Mono<Void> or void", method.getName());
            throw new IllegalArgumentException("Method " + method.getName() + " must return Mono<Void> or void");
        }
    }

    public final void consumeUpdate(Update update) {
        if (update.message() == null) {
            // Possible logic for other update types
            return;
        }
        Message message = update.message();
        for (var handler : messageHandlers) {
            if (handler.filter.test(message)) {
                Mono<Void> resultAsync = handler.handler.apply(message);
                resultAsync
                        .onErrorResume(e -> {
                            log.error("Error in handler", e);
                            return Mono.empty();
                        })
                        .subscribe();
                if (handler.isFinal) {
                    return;
                }
            }
        }
    }

    @PostConstruct
    public void init() {
        Map<Object, List<Method>> methods = getAllHandlers();

        for (var entry : methods.entrySet()) {
            for (Method method : entry.getValue()) {
                if (method.isAnnotationPresent(backend.academy.bot.telegram.utils.annotations.MessageHandler.class)) {
                    registerHandler(entry.getKey(), method);
                }
            }
        }
        messageHandlers.sort(Comparator.comparingInt(MessageHandler::priority));
    }

    private void registerHandler(Object bean, Method method) {
        var messageHandler = method.getAnnotation(backend.academy.bot.telegram.utils.annotations.MessageHandler.class);
        Map<FilterParameter, Object> params = new HashMap<>();
        for (FilterParam param : messageHandler.params()) {
            params.put(param.key(), param.value());
        }

        List<Predicate<Message>> filters = new ArrayList<>();
        for (var filterClass : messageHandler.filters()) {
            MessageFilterGenerator filterGenerator = filterRegister.getFilterInstance(filterClass);
            filters.add(filterGenerator.filter(params));
        }
        messageHandlers.add(new MessageHandler(
                message -> filters.stream().allMatch(filter -> filter.test(message)),
                message -> {
                    try {
                        return handleAsyncOrNotHandler(bean, method, message);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        log.error("Unable to invoke message handler {}", method.getName(), e);
                        throw new RuntimeException("Unable to invoke message handler " + method.getName(), e);
                    }
                },
                messageHandler.priority(),
                messageHandler.isFinal()));
    }

    private Map<Object, List<Method>> getAllHandlers() {
        Map<Object, List<Method>> methods = new HashMap<>();
        String[] beanNames = applicationContext.getBeanNamesForAnnotation(Router.class);
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            List<Method> beanMethods =
                    new ArrayList<>(Arrays.asList(bean.getClass().getMethods()));
            methods.put(bean, beanMethods);
        }
        return methods;
    }

    public record MessageHandler(
            Predicate<Message> filter, Function<Message, Mono<Void>> handler, int priority, boolean isFinal) {}
}
