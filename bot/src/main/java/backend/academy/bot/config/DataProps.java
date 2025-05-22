package backend.academy.bot.config;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.data", ignoreUnknownFields = false)
public record DataProps(@NotEmpty String redis, Integer redisExMs) {
    public DataProps {
        if (redisExMs == null) {
            redisExMs = 60000;
        }
    }
}
