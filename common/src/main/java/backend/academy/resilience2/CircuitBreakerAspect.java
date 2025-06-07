package backend.academy.resilience2;

import java.util.HashMap;
import java.util.Map;

import backend.academy.configuration.ResilienceProps;
import backend.academy.resilience2.impl.CountBasedCircuitBreaker;
import backend.academy.resilience2.utils.ResilienceUtils;
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
@Order(0)
public class CircuitBreakerAspect {
    private final Map<String, backend.academy.resilience2.impl.CircuitBreaker> breakers = new HashMap<>();
    private final ResilienceProps resilienceProps;

    public CircuitBreakerAspect(ResilienceProps resilienceProps) {
        this.resilienceProps = resilienceProps;
        log.info("Circuit breaker aspect initialized");
    }

    @Around(value = "@annotation(CircuitBreaker)", argNames = "joinPoint")
    public Object circuitBreaker(ProceedingJoinPoint joinPoint) {
        log.info("CircuitBreaker aspect triggered for {}", joinPoint.getSignature());
        var breaker = breakers.computeIfAbsent(
                joinPoint.getSignature().toLongString(),
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

    private Mono<?> processMono(
            ProceedingJoinPoint joinPoint, backend.academy.resilience2.impl.CircuitBreaker breaker) {
        return Mono.fromCallable(breaker::process).flatMap(result -> {
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
        });
    }

    private Flux<?> processFlux(
            ProceedingJoinPoint joinPoint, backend.academy.resilience2.impl.CircuitBreaker breaker) {
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

    /**
     * Flush all circuit breakers (mostly for tests)
     */
    public void flush() {
        breakers.clear();
    }
}
