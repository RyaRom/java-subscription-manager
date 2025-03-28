package backend.academy.bot.telegram.utils.middlewares;

import backend.academy.bot.telegram.utils.BotContext;
import backend.academy.bot.telegram.utils.annotations.BotRouterAdvice;
import backend.academy.bot.telegram.utils.annotations.ExceptionHandler;
import backend.academy.bot.telegram.utils.exception.TelegramException;
import com.pengrad.telegrambot.model.Update;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import static backend.academy.bot.telegram.utils.BotContext.getAnnotatedMethods;

@Component
@Log4j2
@RequiredArgsConstructor
public class ExceptionHandlerInterceptor implements AbstractMiddleware<Update, Void> {
    @Getter
    private final Map<Class<? extends Throwable>, BiFunction<TelegramException, Update, Mono<Void>>> errorHandlers =
        new HashMap<>();
    private final ApplicationContext applicationContext;


    @Override
    public Mono<Update> preHandle(Mono<Update> chain) {
        return chain;
    }

    @Override
    public Mono<Void> postHandle(Mono<Void> chain) {
        return chain.onErrorResume(error -> {
            log.info("Error caught {}. class = {}", error.getMessage(), error.getClass());
            TelegramException exception;
            try {
                exception = (TelegramException) error;
            } catch (Throwable e) {
                log.error("Exception was not casted correctly. It needs to be fixed immediately");
                SpringApplication.exit(applicationContext, () -> 1);
                throw new Error();
            }
            var handlerFabric = getEscalatedByExceptionAncestorsResult(error.getClass());
            if (handlerFabric == null){
                return Mono.empty();
            }
            return handlerFabric.apply(exception, exception.getUpdate());
        });
    }

    @Nullable
    private BiFunction<TelegramException, Update, Mono<Void>> getEscalatedByExceptionAncestorsResult
        (Class<?> exceptionClass){
        while (exceptionClass != null && exceptionClass != Throwable.class) {
            for (Class<? extends Throwable> registeredClass : errorHandlers.keySet()) {
                if (registeredClass.isAssignableFrom(exceptionClass)) {
                    return errorHandlers.get(registeredClass);
                }
            }
            exceptionClass = exceptionClass.getSuperclass();
        }
        return null;
    }

    @PostConstruct
    public void init() {
        Map<Object, List<Method>> methods = getAnnotatedMethods(
            applicationContext,
            BotRouterAdvice.class,
            ExceptionHandler.class
        );
        log.info("found handlers: {}", methods);

        for (var entry : methods.entrySet()) {
            for (Method method : entry.getValue()) {
                registerHandler(entry.getKey(), method);
            }
        }

        log.info("registered handlers: {}", errorHandlers);
    }

    private void registerHandler(Object bean, Method method) {
        var annotation = method.getAnnotation(backend.academy.bot.telegram.utils.annotations.ExceptionHandler.class);
        Arrays.stream(annotation.value())
            .forEach(errorClass -> {
                // Possible alternative logic for chaining handlers
//                    errorHandlers.put(
//                        errorClass,
//                        errorHandlers.getOrDefault(errorClass, Mono.empty())
//                            .then(BotContext.handleAsyncOrNotConsumer(bean, method, errorClass))
//                    );
                errorHandlers.putIfAbsent(errorClass, (exception, update) -> {
                    try {
                        return BotContext.handleAsyncOrNotConsumer(bean, method, exception.getCause(), update);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        log.error("Unable to invoke message handler {}", method.getName(), e);
                        throw new RuntimeException("Unable to invoke message handler " + method.getName(), e);
                    }
                });
            });
    }
}
