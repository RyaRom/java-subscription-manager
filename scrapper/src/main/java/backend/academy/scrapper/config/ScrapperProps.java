package backend.academy.scrapper.config;

import backend.academy.configuration.AppConfig;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.validation.annotation.Validated;

@Validated
@Import(AppConfig.class)
@ConfigurationProperties(prefix = "app.main", ignoreUnknownFields = false)
public record ScrapperProps(@Nullable StackOverflowCredentials stackOverflow, @NotEmpty String updateCron) {
    public record StackOverflowCredentials(
            @Nullable String key, @Nullable String accessToken, @NotEmpty Boolean tokenDisabled) {}
}
