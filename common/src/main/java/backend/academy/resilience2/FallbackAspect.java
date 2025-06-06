package backend.academy.resilience2;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import backend.academy.resilience2.impl.ReactorFallback;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Aspect
@Log4j2
@Component
@RequiredArgsConstructor
// trigger after everything is done
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FallbackAspect {
    private final Map<String, Method> nameToMethod = new HashMap<>();

    @Around(value = "@annotation(Fallback)", argNames = "joinPoint")
    public Object fallback(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("Fallback aspect triggered for {}", joinPoint.getSignature());
        var args = joinPoint.getArgs();
        var fallback = findFallback(joinPoint);
        var method = nameToMethod.computeIfAbsent(fallback.value(), name ->
                ReactorFallback.findMethod(joinPoint, name, args));
        Object chain = joinPoint.proceed();

        if (chain instanceof Mono mono) {
            Mono<Object> fallbackMono = ReactorFallback.getFallbackMono(joinPoint, method, args);
            return mono.onErrorResume(e -> fallbackMono);
        } else if (chain instanceof Flux flux) {
            Flux<Object> fallbackFlux = ReactorFallback.getFallbackFlux(joinPoint, method, args);
            return flux.onErrorResume(e -> fallbackFlux);
        }
        throw new RuntimeException("Fallback aspect only supports Mono and Flux");
    }

    private Fallback findFallback(ProceedingJoinPoint joinPoint) {
        //could be bound in aspect method if spring aop wasn't that randomly broken
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod().getAnnotation(Fallback.class);
    }
}
