package backend.academy.bot.config;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.clients", ignoreUnknownFields = false)
public record BotClientsProps(@NotEmpty String scrapperUrl) {
}
