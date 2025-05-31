package backend.academy.configuration;

import backend.academy.resilience.RateLimitingAspect;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    RateLimitingAspect.class,
})
public class AspectModulesConfig {
}
