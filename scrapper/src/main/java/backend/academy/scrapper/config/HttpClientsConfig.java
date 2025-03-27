package backend.academy.scrapper.config;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;

@Validated
@ConfigurationProperties(prefix = "app.clients", ignoreUnknownFields = false)
public record HttpClientsConfig(
    @Nullable String githubToken, @NotEmpty String botUrl,
    String stackOverflowUrl, String githubUrl
) {
    public static final String GITHUB_API_VERSION = "X-GitHub-Api-Version";

    @Bean
    public WebClient githubHttpClient() {
        var builder = WebClient.builder()
            .baseUrl(githubUrl)
            .defaultHeader(GITHUB_API_VERSION, "2022-11-28")
            .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json");
        if (githubToken != null) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + githubToken);
        }
        return builder.build();
    }

    @Bean
    public WebClient stackOverflowHttpClient() {
        var builder = WebClient.builder().baseUrl(stackOverflowUrl);
        return builder.build();
    }

    @Bean
    public WebClient botHttpClient() {
        return WebClient.builder()
            .baseUrl(botUrl)
            .defaultHeader("Content-Type", "application/json")
            .build();
    }
}
