package backend.academy.bot.config;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.clients", ignoreUnknownFields = false)
public record ClientsProps(@NotEmpty String scrapperUrl, Integer timeout, Retry retry) {
    public ClientsProps {
        timeout = 3000;
        retry = new Retry(2, 100L);
    }

    public record Retry(Integer maxAttempts, Long waitDuration) {}
}
