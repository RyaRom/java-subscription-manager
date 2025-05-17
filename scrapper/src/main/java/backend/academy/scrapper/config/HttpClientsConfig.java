package backend.academy.scrapper.config;

import static backend.academy.configuration.CustomHeaders.GITHUB_API_VERSION;

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
        @Nullable String githubToken,
        @NotEmpty String botUrl,
        @NotEmpty String stackOverflowUrl,
        @NotEmpty String githubUrl) {

    @Bean
    public WebClient githubWebClient() {
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
    public WebClient stackOverflowWebClient() {
        var builder = WebClient.builder().baseUrl(stackOverflowUrl);
        return builder.build();
    }

    @Bean
    public WebClient botWebClient() {
        return WebClient.builder()
                .baseUrl(botUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
