package backend.academy.configuration;

import jakarta.annotation.Nullable;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "resilience2")
public record ResilienceProps(
    @Nullable Retry retry,
    @Nullable RateLimiter rateLimiter
) {

    public record Retry(
        Integer maxAttempts,
        Long waitDuration,
        List<Integer> blacklistedStatusCodes
    ) {
        public Retry {
            maxAttempts = maxAttempts != null ? maxAttempts : 3;
            waitDuration = waitDuration != null ? waitDuration : 1000;
            blacklistedStatusCodes = blacklistedStatusCodes != null
                ? blacklistedStatusCodes : List.of(400, 401, 403, 404, 405);
        }
    }

    public record RateLimiter(
        Integer maxTokens,
        Integer tokensPerSecond
    ) {
        public RateLimiter {
            maxTokens = maxTokens != null ? maxTokens : 10;
            tokensPerSecond = tokensPerSecond != null ? tokensPerSecond : 1;
        }
    }
}
