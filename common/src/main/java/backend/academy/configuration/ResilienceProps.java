package backend.academy.configuration;

import java.util.List;
import jakarta.annotation.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "resilience2")
public record ResilienceProps(@Nullable Retry retry) {

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
}
