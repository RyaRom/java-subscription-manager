package backend.academy.scrapper.config;

import backend.academy.scrapper.clients.BotClient;
import backend.academy.scrapper.clients.BotHttpClient;
import backend.academy.scrapper.clients.BotKafkaClient;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.time.Duration;
import javax.naming.ConfigurationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;
import static backend.academy.configuration.CustomHeaders.GITHUB_API_VERSION;

@Log4j2
@Configuration
@RequiredArgsConstructor
public class ClientsConfig {
    private final ClientsProps clientsProps;

    @Bean
    public WebClient githubWebClient() {
        HttpClient httpClient = HttpClient.create()
            .responseTimeout(Duration.ofMillis(clientsProps.timeout()));
        var builder = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .baseUrl(clientsProps.githubUrl())
            .defaultHeader(GITHUB_API_VERSION, "2022-11-28")
            .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json");
        if (clientsProps.githubToken() != null) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION,
                "Bearer " + clientsProps.githubToken());
        }
        return builder.build();
    }

    @Bean
    public WebClient stackOverflowWebClient() {
        HttpClient httpClient = HttpClient.create()
            .responseTimeout(Duration.ofMillis(clientsProps.timeout()));
        var builder = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .baseUrl(clientsProps.stackOverflowUrl());
        return builder.build();
    }

    @Bean
    public WebClient botWebClient() {
        HttpClient httpClient = HttpClient.create()
            .responseTimeout(Duration.ofMillis(clientsProps.timeout()));
        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .baseUrl(clientsProps.botUrl())
            .defaultHeader("Content-Type", "application/json")
            .build();
    }

    @Bean
    public BotClient botClient(WebClient botWebClient, KafkaTemplate<Object, Object> kafkaTemplate)
        throws ConfigurationException {
        if (clientsProps.clientType() == null ||
            clientsProps.clientType().equalsIgnoreCase("http")) {
            return new BotHttpClient(botWebClient);
        } else if (clientsProps.clientType().equalsIgnoreCase("kafka")) {
            return new BotKafkaClient(kafkaTemplate);
        } else {
            throw new ConfigurationException("Unknown client type " + clientsProps.clientType());
        }
    }

    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
            .maxAttempts(clientsProps.retry().maxAttempts())
            .waitDuration(Duration.ofMillis(clientsProps.retry().waitDuration()))
            .retryOnException(e -> {
                log.info("Got error {} : {} in request", e.getMessage(), e);
                if (e instanceof WebClientResponseException responseException) {
                    log.info("Error {} status {}",
                        responseException.getMessage(),
                        responseException.getStatusCode());
                    return responseException.getStatusCode().is5xxServerError()
                        || responseException.getStatusCode().value() == 429;
                }
                return e instanceof WebClientRequestException;
            })
            .build();

        return RetryRegistry.of(config);
    }
}
