package backend.academy.bot.config;

import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.clients.ScrapperClientCached;
import backend.academy.bot.clients.ScrapperHttpClient;
import backend.academy.bot.clients.ScrapperPublisher;
import backend.academy.bot.clients.ScrapperPublisherCached;
import backend.academy.proto.impl.Links;
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
import reactor.netty.http.client.HttpClient;

@Profile({"dev", "testing"})
@Log4j2
@RequiredArgsConstructor
@Configuration
public class ClientsConfig {
    private final ClientsProps clientsProps;

    @Bean
    public WebClient scrapperWebClient() {
        HttpClient httpClient = HttpClient.create().responseTimeout(Duration.ofMillis(clientsProps.timeout()));
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(clientsProps.scrapperUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public WebClient botHttpClient(TelegramProps telegramProps) {
        HttpClient httpClient = HttpClient.create().responseTimeout(Duration.ofMillis(clientsProps.timeout()));
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl("https://api.telegram.org/bot" + telegramProps.telegramToken())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    @Profile({"dev"})
    public ScrapperClient scrapperClient(
            DataProps dataProps,
            ScrapperHttpClient scrapperHttpClient,
            RedisReactiveCommands<String, Links.ListLinksProto> redisReactiveCommandsProto) {
        return new ScrapperClientCached(dataProps, scrapperHttpClient, redisReactiveCommandsProto);
    }

    @Bean
    @Profile({"dev"})
    public ScrapperPublisher scrapperPublisher(
            ScrapperHttpClient scrapperHttpClient,
            RedisReactiveCommands<String, Links.ListLinksProto> redisReactiveCommandsProto) {
        return new ScrapperPublisherCached(scrapperHttpClient, redisReactiveCommandsProto);
    }
}
