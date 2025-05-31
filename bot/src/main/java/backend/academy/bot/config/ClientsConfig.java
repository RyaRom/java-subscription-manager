package backend.academy.bot.config;

import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.clients.ScrapperClientCached;
import backend.academy.bot.clients.ScrapperHttpClient;
import backend.academy.bot.clients.ScrapperPublisher;
import backend.academy.bot.clients.ScrapperPublisherCached;
import backend.academy.proto.impl.Links;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

@Profile({"dev", "testing"})
@Log4j2
@RequiredArgsConstructor
@Configuration
public class ClientsConfig {
    private final ClientsProps clientsProps;

    @Bean
    public WebClient scrapperWebClient() {
        HttpClient httpClient = HttpClient.create()
            .responseTimeout(Duration.ofMillis(clientsProps.timeout()));
        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .baseUrl(clientsProps.scrapperUrl())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    @Bean
    public WebClient botHttpClient(String telegramToken) {
        HttpClient httpClient = HttpClient.create()
            .responseTimeout(Duration.ofMillis(clientsProps.timeout()));
        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .baseUrl("https://api.telegram.org/bot" + telegramToken)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    @Bean
    public ScrapperClient scrapperClient(
            DataProps dataProps,
            WebClient scrapperWebClient,
            RedisReactiveCommands<String, Links.ListLinksProto> redisReactiveCommandsProto) {
        return new ScrapperClientCached(
                dataProps, new ScrapperHttpClient(scrapperWebClient), redisReactiveCommandsProto);
    }

    @Bean
    public ScrapperPublisher scrapperPublisher(
            WebClient scrapperWebClient,
            RedisReactiveCommands<String, Links.ListLinksProto> redisReactiveCommandsProto) {
        return new ScrapperPublisherCached(new ScrapperHttpClient(scrapperWebClient), redisReactiveCommandsProto);
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
