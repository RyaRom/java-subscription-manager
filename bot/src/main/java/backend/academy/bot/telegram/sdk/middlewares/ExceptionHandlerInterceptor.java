package backend.academy.bot.telegram.sdk.middlewares;

import static backend.academy.bot.telegram.sdk.BotContext.getAnnotatedMethods;

import backend.academy.bot.telegram.sdk.BotContext;
import backend.academy.bot.telegram.sdk.annotations.BotRouterAdvice;
import backend.academy.bot.telegram.sdk.annotations.ExceptionHandler;
import backend.academy.bot.telegram.sdk.utils.TelegramException;
import com.pengrad.telegrambot.model.Update;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Log4j2
@RequiredArgsConstructor
public class ExceptionHandlerInterceptor implements AbstractMiddleware {
    @Getter
    private final Map<Class<? extends Throwable>, BiFunction<Throwable, Update, Mono<Update>>> errorHandlers =
            new HashMap<>();

    private final ApplicationContext applicationContext;

    @Override
    public Mono<Update> preHandle(Mono<Update> chain) {
        return chain;
    }

    @Override
    public Mono<Update> postHandle(Mono<Update> chain) {
        return chain.onErrorResume((exception) -> {
            var casted = ((TelegramException) exception);
            log.info("Error caught {}. class = {}", casted.getReason(), casted.getReason());
            var handlerFabric =
                    getEscalatedByExceptionAncestorsResult(casted.getReason().getClass());
            if (handlerFabric == null) {
                return Mono.empty();
            }
            return handlerFabric.apply(casted.getReason(), casted.getUpdate());
        });
    }

    @Nullable
    private BiFunction<Throwable, Update, Mono<Update>> getEscalatedByExceptionAncestorsResult(
            Class<?> exceptionClass) {
        while (exceptionClass != null && exceptionClass != Throwable.class) {
            log.info("Trying to find handler for {}", exceptionClass);
            var found = errorHandlers.get(exceptionClass);
            if (found != null) {
                return found;
            }
            exceptionClass = exceptionClass.getSuperclass();
        }
        return null;
    }

    @PostConstruct
    public void init() {
        Map<Object, List<Method>> methods =
                getAnnotatedMethods(applicationContext, BotRouterAdvice.class, ExceptionHandler.class);
        log.info("found handlers: {}", methods);

        for (var entry : methods.entrySet()) {
            for (Method method : entry.getValue()) {
                registerHandler(entry.getKey(), method);
            }
        }

        log.info("registered handlers: {}", errorHandlers);
    }

    private void registerHandler(Object bean, Method method) {
        var annotation = method.getAnnotation(ExceptionHandler.class);
        Arrays.stream(annotation.value()).forEach(errorClass -> {
            // Possible alternative logic for chaining handlers
            //                    errorHandlers.put(
            //                        errorClass,
            //                        errorHandlers.getOrDefault(errorClass, Mono.empty())
            //                            .then(BotContext.handleAsyncOrNotConsumer(bean, method, errorClass))
            //                    );
            errorHandlers.putIfAbsent(errorClass, (exception, update) -> {
                try {
                    return BotContext.handleAsyncOrNotFunction(bean, method, Void.TYPE, exception, update)
                            .thenReturn(update);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    log.error("Unable to invoke message handler {}, {}", method.getName(), e);
                    throw new RuntimeException(e);
                }
            });
        });
    }
}
