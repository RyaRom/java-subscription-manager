package backend.academy.resilience2;

import backend.academy.resilience2.impl.ReactorFallback;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
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
        Object chain = joinPoint.proceed();

        Mono<Object> fallbackMono = ReactorFallback.getFallbackMono(joinPoint, method);
        if (chain instanceof Mono mono) {
            return mono.onErrorResume(e -> fallbackMono);
        } else if (chain instanceof Flux flux) {
            return flux.onErrorResume(e -> fallbackMono);
        }
        throw new RuntimeException("Fallback aspect only supports Mono and Flux");
    }
}
