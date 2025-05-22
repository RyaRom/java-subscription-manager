package backend.academy.scrapper.config;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.data", ignoreUnknownFields = false)
public record DataConnectionProperties(
    @NotEmpty String type,
    @NotEmpty String redis,
    Integer redisExMs,
    Integer linksPagesize
) {
    public DataConnectionProperties {
        if (redisExMs == null) {
            redisExMs = 60 * 60;
        }
        if (linksPagesize == null) {
            linksPagesize = 100;
        }
    }
}
