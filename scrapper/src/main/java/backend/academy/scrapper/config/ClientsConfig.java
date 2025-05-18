package backend.academy.scrapper.config;

import backend.academy.scrapper.clients.BotClient;
import backend.academy.scrapper.clients.BotHttpClient;
import backend.academy.scrapper.clients.BotKafkaClient;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import javax.naming.ConfigurationException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;
import static backend.academy.configuration.CustomHeaders.GITHUB_API_VERSION;

@Validated
@ConfigurationProperties(prefix = "app.clients", ignoreUnknownFields = false)
public record ClientsConfig(
    @Nullable String githubToken,
    @NotEmpty String botUrl,
    @NotEmpty String stackOverflowUrl,
    @NotEmpty String githubUrl,
    @Nullable String botClientType
) {

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

    @Bean
    public BotClient botClient(
        WebClient botWebClient,
        KafkaTemplate<Object, Object> kafkaTemplate
    ) throws ConfigurationException {
        if (botClientType == null || botClientType.equalsIgnoreCase("http")) {
            return new BotHttpClient(botWebClient);
        } else if (botClientType.equalsIgnoreCase("kafka")) {
            return new BotKafkaClient(kafkaTemplate);
        } else {
            throw new ConfigurationException("Unknown client type " + botClientType);
        }
    }
}
