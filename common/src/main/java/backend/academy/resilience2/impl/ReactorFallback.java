package backend.academy.resilience2.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@Log4j2
public class ReactorFallback {
    public static @NotNull Mono<Object> getFallbackMono(ProceedingJoinPoint joinPoint, Method method) {
        if (method.getReturnType() == Mono.class) {
            try {
                return (Mono<Object>) method.invoke(joinPoint.getTarget());
            } catch (IllegalAccessException | InvocationTargetException ex) {
                throw new RuntimeException("Fallback execution failed", ex);
            }
        }
        log.error("Fallback aspect triggered for {}", joinPoint.getSignature());
        return Mono.fromRunnable(() -> {
            try {
                method.invoke(joinPoint.getTarget());
            } catch (IllegalAccessException | InvocationTargetException ex) {
                throw new RuntimeException("Fallback execution failed", ex);
            }
        });
    }
}
