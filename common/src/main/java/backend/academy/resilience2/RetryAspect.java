package backend.academy.resilience2;

import backend.academy.resilience2.impl.RetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Aspect
@Log4j2
@Component
@Order(1)
public class RetryAspect {
    private final RetryService defaultRetry;

    public RetryAspect(RetryService defaultRetry) {
        log.info("RetryAspect initialized");
        this.defaultRetry = defaultRetry;
    }

    @Around("@annotation(retry)")
    public Object retry(ProceedingJoinPoint joinPoint, Retry retry) throws Throwable {
        log.info("RateLimit aspect triggered for {}", joinPoint.getSignature());
        Mono<?> originalMono = (Mono<?>) joinPoint.proceed();

        return originalMono
            .transformDeferred(defaultRetry::withRetry)
            .doOnNext(val -> log.info("Request completed successfully"));
    }
}
