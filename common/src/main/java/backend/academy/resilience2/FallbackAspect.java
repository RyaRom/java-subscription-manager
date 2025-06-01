package backend.academy.resilience2;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Aspect
@Log4j2
@Component
@RequiredArgsConstructor
public class FallbackAspect {
    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        log.info("RateLimit aspect triggered for {}", joinPoint.getSignature());

        Mono<?> originalMono = (Mono<?>) joinPoint.proceed();

        return originalMono
//            .doOnSubscribe(sub -> log.info("Applying rate limiting with {} permits", rateLimit.limitForWindow()))
            .doOnNext(val -> log.info("Request completed successfully"));
    }
}
