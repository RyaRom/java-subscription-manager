package backend.academy.scrapper.config;

import static backend.academy.configuration.GlobalConstants.GITHUB_API_VERSION;

import backend.academy.scrapper.clients.BotClient;
import backend.academy.scrapper.clients.BotClientProxy;
import backend.academy.scrapper.clients.BotHttpClient;
import backend.academy.scrapper.clients.BotKafkaClient;
import java.time.Duration;
import javax.naming.ConfigurationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Log4j2
@Configuration
@RequiredArgsConstructor
public class ClientsConfig {
    private final ClientsProps clientsProps;

    @Bean
    public WebClient githubWebClient() {
        HttpClient httpClient = HttpClient.create().responseTimeout(Duration.ofMillis(clientsProps.timeout()));
        var builder = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(clientsProps.githubUrl())
                .defaultHeader(GITHUB_API_VERSION, "2022-11-28")
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json");
        if (clientsProps.githubToken() != null) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + clientsProps.githubToken());
        }
        return builder.build();
    }

    @Bean
    public WebClient stackOverflowWebClient() {
        HttpClient httpClient = HttpClient.create().responseTimeout(Duration.ofMillis(clientsProps.timeout()));
        var builder = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(clientsProps.stackOverflowUrl());
        return builder.build();
    }

    @Bean
    public WebClient botWebClient() {
        HttpClient httpClient = HttpClient.create().responseTimeout(Duration.ofMillis(clientsProps.timeout()));
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(clientsProps.botUrl())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public BotClient botClient(BotHttpClient botHttpClient, BotKafkaClient botKafkaClient)
            throws ConfigurationException {
        BotClient delegate;
        if (clientsProps.clientType() == null || clientsProps.clientType().equalsIgnoreCase("http")) {
            delegate = botHttpClient;
        } else if (clientsProps.clientType().equalsIgnoreCase("kafka")) {
            delegate = botKafkaClient;
        } else {
            throw new ConfigurationException("Unknown delegate type " + clientsProps.clientType());
        }
        return new BotClientProxy(botKafkaClient, botHttpClient, delegate);
    }
}
