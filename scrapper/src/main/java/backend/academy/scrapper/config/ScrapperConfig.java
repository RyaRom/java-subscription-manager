package backend.academy.scrapper.config;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ScrapperConfig(
    StackOverflowCredentials stackOverflow
) {

    @Bean
    public StackOverflowCredentials stackOverflowCredentials() {
        return stackOverflow;
    }

    public record StackOverflowCredentials(
        @Nullable String key, @Nullable String accessToken, @NotEmpty Boolean tokenDisabled) {
    }
}
