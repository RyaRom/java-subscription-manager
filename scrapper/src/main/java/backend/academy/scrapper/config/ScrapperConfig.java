package backend.academy.scrapper.config;

import backend.academy.configuration.AppConfig;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.validation.annotation.Validated;

@Validated
@Import(AppConfig.class)
@ConfigurationProperties(prefix = "app.main", ignoreUnknownFields = false)
public record ScrapperConfig(@Nullable StackOverflowCredentials stackOverflow, @NotEmpty String updateCron) {
    @Bean
    public StackOverflowCredentials stackOverflowCredentials() {
        return stackOverflow;
    }

    @Bean
    public String updateCron() {
        return updateCron;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .activateDefaultTyping(
                        BasicPolymorphicTypeValidator.builder()
                                .allowIfSubType("com.yourpackage")
                                .build(),
                        ObjectMapper.DefaultTyping.NON_FINAL,
                        JsonTypeInfo.As.PROPERTY)
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    public record StackOverflowCredentials(
            @Nullable String key, @Nullable String accessToken, @NotEmpty Boolean tokenDisabled) {}
}
