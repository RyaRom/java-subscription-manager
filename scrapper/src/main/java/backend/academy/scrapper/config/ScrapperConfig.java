package backend.academy.scrapper.config;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ScrapperConfig(
        @NotEmpty String githubToken, StackOverflowCredentials stackOverflow, @NotEmpty String botUrl) {
    @Bean
    @Qualifier("githubHttpClient")
    public WebClient githubHttpClient() {
        return WebClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Authorization", "Bearer " + githubToken)
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .defaultHeader("Accept", "application/vnd.github+json")
                .build();
    }

    @Bean
    @Qualifier("botHttpClient")
    public WebClient botHttpClient() {
        return WebClient.builder()
                .baseUrl(botUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    @Qualifier("stackoverflowHttpClient")
    public WebClient stackoverflowHttpClient() {
        return WebClient.builder().baseUrl("https://api.stackexchange.com").build();
    }

    public record StackOverflowCredentials(@NotEmpty String key, @NotEmpty String accessToken) {}
}
