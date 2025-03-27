package backend.academy.bot.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;

@Validated
@Log4j2
@ConfigurationProperties(prefix = "app.clients", ignoreUnknownFields = false)
public record BotClientsConfig(@NotEmpty String scrapperUrl) {
    @Bean
    public WebClient scrapperHttpClient() {
        return WebClient.builder()
            .baseUrl(scrapperUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    @Bean
    public WebClient botHttpClient(String telegramToken) {
        return WebClient.builder()
            .baseUrl("https://api.telegram.org/bot" + telegramToken)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
}
