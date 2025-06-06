package backend.academy.resilience2.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.jetbrains.annotations.NotNull;
import org.springframework.aop.support.AopUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
public class ReactorFallback {
    public static @NotNull Mono<Object> getFallbackMono(ProceedingJoinPoint joinPoint, Method method, Object[] args) {
        if (method.getReturnType() == Mono.class) {
            try {
                return (Mono<Object>) method.invoke(joinPoint.getTarget(), args);
            } catch (IllegalAccessException | InvocationTargetException ex) {
                throw new RuntimeException("Fallback execution failed", ex);
            }
        }
        log.error("Fallback aspect triggered for {}", joinPoint.getSignature());
        return Mono.fromRunnable(() -> {
            try {
                method.invoke(joinPoint.getTarget(), args);
            } catch (IllegalAccessException | InvocationTargetException ex) {
                throw new RuntimeException("Fallback execution failed", ex);
            }
        });
    }

    public static @NotNull Flux<Object> getFallbackFlux(ProceedingJoinPoint joinPoint, Method method, Object[] args) {
        if (method.getReturnType() == Flux.class) {
            try {
                return (Flux<Object>) method.invoke(joinPoint.getTarget(), args);
            } catch (IllegalAccessException | InvocationTargetException ex) {
                throw new RuntimeException("Fallback execution failed", ex);
            }
        }
        log.error("Fallback aspect triggered for {}", joinPoint.getSignature());
        return Flux.fromIterable(List.of());
    }

    public static Method findMethod(JoinPoint joinPoint, String methodName, Object[] args) {
        Class<?> target = AopUtils.getTargetClass(joinPoint.getTarget());
        try {
            return target.getMethod(
                    methodName, Arrays.stream(args).map(Object::getClass).toArray(Class[]::new));
        } catch (NoSuchMethodException e) {
            log.error("Fallback method {} {} not found", methodName, Arrays.toString(args));
            throw new FallbackException("Fallback method not found");
        }
    }

    public static class FallbackException extends RuntimeException {
        public FallbackException(String message) {
            super(message);
        }
    }
}
