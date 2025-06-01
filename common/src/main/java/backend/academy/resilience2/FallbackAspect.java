package backend.academy.resilience2;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Aspect
@Log4j2
@Component
@RequiredArgsConstructor
public class FallbackAspect {
    private final Map<String, Method> nameToMethod = new HashMap<>();

    @Around("@annotation(fallback)")
    public Object fallback(ProceedingJoinPoint joinPoint, Fallback fallback) throws Throwable {
        log.info("Fallback aspect triggered for {}", joinPoint.getSignature());
        var method = nameToMethod.computeIfAbsent(fallback.value(), name -> {
            try {
                return joinPoint.getTarget().getClass().getMethod(name);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });

        Mono originalMono = (Mono) joinPoint.proceed();

        return originalMono
            .onErrorResume(e -> getFallback(joinPoint, method));
    }

    private static @NotNull Mono<Object> getFallback(ProceedingJoinPoint joinPoint, Method method) {
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

