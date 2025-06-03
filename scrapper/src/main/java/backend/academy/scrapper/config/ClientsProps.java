package backend.academy.scrapper.config;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.clients", ignoreUnknownFields = false)
public record ClientsProps(
        @Nullable String githubToken,
        @NotEmpty String botUrl,
        @NotEmpty String stackOverflowUrl,
        @NotEmpty String githubUrl,
        @Nullable String clientType,
        Integer timeout,
        Retry retry) {
    public ClientsProps {
        timeout = 3000;
        retry = new Retry(2, 100L);
    }

    public record Retry(Integer maxAttempts, Long waitDuration) {}
}
