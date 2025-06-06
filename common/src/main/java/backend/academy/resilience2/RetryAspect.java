package backend.academy.resilience2;

import backend.academy.resilience2.impl.ReactorRetrier;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Aspect
@Log4j2
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RetryAspect {
    private final ReactorRetrier defaultRetry;

    public RetryAspect(ReactorRetrier defaultRetry) {
        log.info("RetryAspect initialized");
        this.defaultRetry = defaultRetry;
    }

    @Around(value = "@annotation(Retry)", argNames = "joinPoint")
    public Object retry(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("Retry aspect triggered for {}", joinPoint.getSignature());
        var chain = joinPoint.proceed();
        if (chain instanceof Mono<?> mono) {
            return mono.transformDeferred(defaultRetry::withRetry);
        } else if (chain instanceof Flux<?> flux) {
            return flux.transformDeferred(defaultRetry::withRetry);
        }
        throw new IllegalStateException("Unsupported chain type");
    }
}
