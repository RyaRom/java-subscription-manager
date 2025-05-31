package backend.academy.bot.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@Log4j2
@ConfigurationProperties(prefix = "app.telegram", ignoreUnknownFields = false)
public record TelegramProps(@NotEmpty String telegramToken) {
}
