package backend.academy.configuration;

import jakarta.annotation.Nullable;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "resilience2")
public record ResilienceProps(
    @Nullable Retry retry,
    @Nullable RateLimiter rateLimiter,
    @Nullable CircuitBreaker circuitBreaker
) {

    public record Retry(Integer maxAttempts, Long waitDuration, List<Integer> blacklistedStatusCodes) {
        public Retry {
            maxAttempts = maxAttempts != null ? maxAttempts : 3;
            waitDuration = waitDuration != null ? waitDuration : 1000;
            blacklistedStatusCodes =
                blacklistedStatusCodes != null ? blacklistedStatusCodes : List.of(400, 401, 403, 404, 405);
        }
    }

    public record RateLimiter(Integer maxTokens, Integer tokensPerSecond) {
        public RateLimiter {
            maxTokens = maxTokens != null ? maxTokens : 10;
            tokensPerSecond = tokensPerSecond != null ? tokensPerSecond : 1;
        }
    }

    public record CircuitBreaker(
        Integer initCalls,
        Integer windowSize,
        Integer failureRateThreshold,
        Integer halfOpenCallsPermitted,
        Integer waitDurationInOpenStateMs
    ) {
        public CircuitBreaker {
            initCalls = initCalls != null ? initCalls : 10;
            windowSize = windowSize != null ? windowSize : 100;
            failureRateThreshold = failureRateThreshold != null ? failureRateThreshold : 50;
            halfOpenCallsPermitted = halfOpenCallsPermitted != null ? halfOpenCallsPermitted : 10;
            waitDurationInOpenStateMs = waitDurationInOpenStateMs != null ? waitDurationInOpenStateMs : 5000;
        }
    }
}
