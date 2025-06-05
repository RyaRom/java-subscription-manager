package backend.academy.resilience2;

import backend.academy.configuration.ResilienceProps;
import backend.academy.resilience2.impl.CountBasedCircuitBreaker;
import backend.academy.resilience2.utils.ResilienceUtils;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Aspect
@Log4j2
@Component
@Order(2)
@RequiredArgsConstructor
public class CircuitBreakerAspect {
    private final Map<String, backend.academy.resilience2.impl.CircuitBreaker> breakers =
        new HashMap<>();
    private final ResilienceProps resilienceProps;

    @Around("@annotation(circuitBreaker)")
    public Object circuitBreaker(ProceedingJoinPoint joinPoint, CircuitBreaker circuitBreaker) throws Throwable {
        log.info("CircuitBreaker aspect triggered for {}", joinPoint.getSignature());
        var breaker = breakers.computeIfAbsent(joinPoint.getSignature().toLongString(),
            key -> new CountBasedCircuitBreaker(resilienceProps.circuitBreaker()));
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        var returnType = signature.getReturnType();
        if (returnType.equals(Mono.class)) {
            return processMono(joinPoint, breaker);
        } else if (returnType.equals(Flux.class)) {
            return processFlux(joinPoint, breaker);
        }
        throw new IllegalStateException("Unsupported chain type");
    }

    private Object processMono(
        ProceedingJoinPoint joinPoint,
        backend.academy.resilience2.impl.CircuitBreaker breaker
    ) {
        boolean result = breaker.process();
        if (result) {
            Mono<?> mono;
            try {
                mono = (Mono<?>) joinPoint.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return breaker.addOnErrorCallback(mono);
        } else {
            return Mono.error(ResilienceUtils.getTooManyRequests(
                resilienceProps.circuitBreaker().waitDurationInOpenStateMs()));
        }
    }

    private Object processFlux(
        ProceedingJoinPoint joinPoint,
        backend.academy.resilience2.impl.CircuitBreaker breaker
    ) {
        boolean result = breaker.process();
        if (result) {
            Flux<?> flux;
            try {
                flux = (Flux<?>) joinPoint.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return breaker.addOnErrorCallback(flux);
        } else {
            return Flux.error(ResilienceUtils.getTooManyRequests(
                resilienceProps.circuitBreaker().waitDurationInOpenStateMs()));
        }
    }
}
