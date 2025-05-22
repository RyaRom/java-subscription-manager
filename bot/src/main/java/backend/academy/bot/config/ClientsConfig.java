package backend.academy.bot.config;

import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.clients.ScrapperClientCached;
import backend.academy.bot.clients.ScrapperHttpClient;
import backend.academy.bot.clients.ScrapperPublisher;
import backend.academy.bot.clients.ScrapperPublisherCached;
import backend.academy.proto.impl.Links;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Log4j2
@RequiredArgsConstructor
@Configuration
public class ClientsConfig {
    private final BotClientsProps botClientsProps;

    @Bean
    public WebClient scrapperWebClient() {
        return WebClient.builder()
            .baseUrl(botClientsProps.scrapperUrl())
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

    @Bean
    public ScrapperClient scrapperClient(
        DataProps dataProps,
        WebClient scrapperWebClient,
        @Qualifier("redisReactiveCommands")
        RedisReactiveCommands<String, Links.ListLinksProto> redisReactiveCommands
    ) {
        return new ScrapperClientCached(
            dataProps,
            new ScrapperHttpClient(scrapperWebClient),
            redisReactiveCommands
        );
    }

    @Bean
    public ScrapperPublisher scrapperPublisher(
        WebClient scrapperWebClient,
        RedisReactiveCommands<String, Links.ListLinksProto> redisReactiveCommands
    ) {
        return new ScrapperPublisherCached(
            new ScrapperHttpClient(scrapperWebClient),
            redisReactiveCommands
        );
    }
}
