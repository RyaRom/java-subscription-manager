package backend.academy.configuration;

import backend.academy.resilience2.CircuitBreakerAspect;
import backend.academy.resilience2.FallbackAspect;
import backend.academy.resilience2.RateLimitingAspect;
import backend.academy.resilience2.RetryAspect;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    RateLimitingAspect.class,
    RetryAspect.class,
    FallbackAspect.class,
    CircuitBreakerAspect.class,
})
public class AspectModulesConfig {}
