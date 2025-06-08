package backend.academy.resilience2;

import static backend.academy.resilience2.utils.ResilienceUtils.getTooManyRequests;

import backend.academy.configuration.GlobalConstants;
import backend.academy.configuration.ResilienceProps;
import backend.academy.resilience2.impl.InMemoryTokenBucketRateLimiter;
import backend.academy.resilience2.impl.RateLimiter;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

@Aspect
@Log4j2
@Component
@RequiredArgsConstructor
public class RateLimitingAspect {
    // could be done with separated properties for name and get name in annotation
    // for multiple config variations
    private final ResilienceProps resilienceProps;
    private final Map<String, RateLimiter> limiterForEndpoint = new HashMap<>();

    @Around(value = "@annotation(RateLimit)", argNames = "joinPoint")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("RateLimit aspect triggered for {}", joinPoint.getSignature());

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        var returnType = signature.getReturnType();
        if (returnType.equals(Mono.class)) {
            return limitMono(joinPoint);
        } else if (returnType.equals(Flux.class)) {
            return limitFlux(joinPoint);
        }
        // won't have context if not reactive
        throw new IllegalArgumentException("Unsupported return type: " + returnType);
    }

    private Mono<?> limitMono(ProceedingJoinPoint joinPoint) {
        return Mono.deferContextual(contextView -> {
            log.info("BEFORE METHOD IN MONO ASPECT");
            int retryAfterMs = getRetryAfterMs(joinPoint, contextView);
            if (retryAfterMs == -1) {
                try {
                    return (Mono<?>) joinPoint.proceed();
                } catch (Throwable e) {
                    return Mono.error(e);
                }
            }
            return Mono.error(getTooManyRequests(retryAfterMs));
        });
    }

    private Flux<?> limitFlux(ProceedingJoinPoint joinPoint) {
        return Flux.deferContextual(contextView -> {
            log.info("BEFORE METHOD IN FLUX ASPECT");
            int retryAfterMs = getRetryAfterMs(joinPoint, contextView);
            if (retryAfterMs == -1) {
                try {
                    return (Flux<?>) joinPoint.proceed();
                } catch (Throwable e) {
                    return Mono.error(e);
                }
            }
            return Mono.error(getTooManyRequests(retryAfterMs));
        });
    }

    private int getRetryAfterMs(ProceedingJoinPoint joinPoint, ContextView contextView) {
        String ip = contextView.get(GlobalConstants.USER_IP_CONTEXT);
        log.info("User IP: {}", ip);
        RateLimiter limiter = limiterForEndpoint.computeIfAbsent(
                joinPoint.getSignature().toLongString(),
                k -> new InMemoryTokenBucketRateLimiter(resilienceProps.rateLimiter()));
        return limiter.processRequest(ip);
    }

    /** Flush all limiters (mostly for tests) */
    public void flush() {
        limiterForEndpoint.clear();
    }
}
