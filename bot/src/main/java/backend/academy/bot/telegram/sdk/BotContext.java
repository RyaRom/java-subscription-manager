package backend.academy.bot.telegram.sdk;

import backend.academy.bot.telegram.sdk.annotations.FilterParam;
import backend.academy.bot.telegram.sdk.annotations.Router;
import backend.academy.bot.telegram.sdk.filters.FilterParameter;
import backend.academy.bot.telegram.sdk.filters.FilterRegister;
import backend.academy.bot.telegram.sdk.filters.MessageFilter;
import backend.academy.bot.telegram.sdk.middlewares.MiddlewaresContext;
import backend.academy.bot.telegram.sdk.utils.TelegramException;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import jakarta.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
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
public class BotContext {
    private final FilterRegister filterRegister;
    private final ApplicationContext applicationContext;
    private final List<MessageHandler> messageHandlers = new ArrayList<>();
    private final MiddlewaresContext middlewaresContext;

    public static <T> Mono<T> handleAsyncOrNotFunction(Object bean, Method method, Class<T> returnType, Object... args)
            throws IllegalAccessException, InvocationTargetException {
        Object result = method.invoke(bean, args);
        if (result instanceof Mono) {
            return (Mono<T>) result;
        } else {
            log.warn("Blocking call in {}", method.getName());
            return Mono.fromCallable(() -> {
                        var uncasted = method.invoke(bean, args);
                        if (returnType.isInstance(uncasted)) {
                            return (T) returnType;
                        }
                        throw new RuntimeException(
                                "Method " + method.getName() + " must return " + returnType.getName());
                    })
                    .subscribeOn(Schedulers.boundedElastic());
        }
    }

    public static Map<Object, List<Method>> getAnnotatedMethods(
            ApplicationContext applicationContext,
            Class<? extends Annotation> targetAnnotation,
            Class<? extends Annotation> methodAnnotation) {
        Map<Object, List<Method>> methods = new HashMap<>();
        String[] beanNames = applicationContext.getBeanNamesForAnnotation(targetAnnotation);
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            List<Method> beanMethods = new ArrayList<>(
                            Arrays.asList(bean.getClass().getMethods()))
                    .stream()
                            .filter(method -> method.isAnnotationPresent(methodAnnotation))
                            .toList();
            methods.put(bean, beanMethods);
        }
        return methods;
    }

    public void consumeUpdate(Update update) {
        if (update.message() == null) {
            // Possible logic for other update types
            return;
        }
        Message message = update.message();
        for (var handler : messageHandlers) {
            if (handler.filter.test(message)) {
                Mono<Update> pipeline = middlewaresContext.applyMiddlewares(
                        Mono.just(update),
                        handler.handler
                                .apply(message)
                                .thenReturn(update)
                                .onErrorMap(throwable -> new TelegramException(update, throwable)));
                pipeline.subscribe();
                if (handler.isFinal) {
                    return;
                }
            }
        }
    }

    @PostConstruct
    public void init() {
        Map<Object, List<Method>> methods = getAnnotatedMethods(
                applicationContext, Router.class, backend.academy.bot.telegram.sdk.annotations.MessageHandler.class);

        for (var entry : methods.entrySet()) {
            for (Method method : entry.getValue()) {
                registerHandler(entry.getKey(), method);
            }
        }
        messageHandlers.sort(Comparator.comparingInt(MessageHandler::priority));
    }

    private void registerHandler(Object bean, Method method) {
        var messageHandler = method.getAnnotation(backend.academy.bot.telegram.sdk.annotations.MessageHandler.class);
        Map<FilterParameter, Object> params = new HashMap<>();
        for (FilterParam param : messageHandler.params()) {
            params.put(param.key(), param.value());
        }

        List<Predicate<Message>> filters = new ArrayList<>();
        for (var filterClass : messageHandler.filters()) {
            MessageFilter filterGenerator = filterRegister.getMessageFilterInstance(filterClass);
            filters.add(filterGenerator.filter(params));
        }
        messageHandlers.add(new MessageHandler(
                message -> filters.stream().allMatch(filter -> filter.test(message)),
                message -> {
                    try {
                        return handleAsyncOrNotFunction(bean, method, Void.TYPE, message);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        log.error("Unable to invoke message handler {}", method.getName(), e);
                        throw new RuntimeException("Unable to invoke message handler " + method.getName(), e);
                    }
                },
                messageHandler.priority(),
                messageHandler.isFinal()));
    }

    public record MessageHandler(
            Predicate<Message> filter, Function<Message, Mono<Void>> handler, int priority, boolean isFinal) {}
}
