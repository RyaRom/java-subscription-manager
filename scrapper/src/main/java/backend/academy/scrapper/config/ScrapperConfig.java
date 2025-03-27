package backend.academy.scrapper.config;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ScrapperConfig(
    @Nullable String githubToken, StackOverflowCredentials stackOverflow, @NotEmpty String botUrl) {
    @Bean
    public WebClient githubHttpClient() {
        var builder = WebClient.builder()
            .baseUrl("https://api.github.com")
            .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
            .defaultHeader("Accept", "application/vnd.github+json");
        if (githubToken != null) {
            builder.defaultHeader("Authorization", "Bearer " + githubToken);
        }
        return builder.build();
    }

    @Bean
    public WebClient stackOverflowHttpClient() {
        var builder = WebClient.builder().baseUrl("https://api.stackexchange.com/2.3");
        return builder.build();
    }

    @Bean
    public StackOverflowCredentials stackOverflowCredentials() {
        return stackOverflow;
    }

    @Bean
    public WebClient botHttpClient() {
        return WebClient.builder()
            .baseUrl(botUrl)
            .defaultHeader("Content-Type", "application/json")
            .build();
    }

    public record StackOverflowCredentials(
        @Nullable String key, @Nullable String accessToken, @NotEmpty Boolean tokenDisabled) {
    }
}
