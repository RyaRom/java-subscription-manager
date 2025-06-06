package backend.academy.scrapper.config;

import java.time.Duration;

import javax.naming.ConfigurationException;

import backend.academy.scrapper.clients.BotClient;
import backend.academy.scrapper.clients.BotClientProxy;
import backend.academy.scrapper.clients.BotHttpClient;
import backend.academy.scrapper.clients.BotKafkaClient;
import backend.academy.scrapper.resilience.RetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import static backend.academy.configuration.GlobalConstants.GITHUB_API_VERSION;

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
    public BotClient botClient(
            WebClient botWebClient,
            KafkaTemplate<Object, Object> kafkaTemplate,
            RetryService retryService,
            ApplicationContext applicationContext
    )
            throws ConfigurationException {
        if (clientsProps.clientType() == null || clientsProps.clientType().equalsIgnoreCase("http")) {
            return new BotHttpClient(botWebClient, retryService, applicationContext);
        } else if (clientsProps.clientType().equalsIgnoreCase("kafka")) {
            return new BotKafkaClient(kafkaTemplate, applicationContext);
        } else {
            throw new ConfigurationException("Unknown client type " + clientsProps.clientType());
        }
    }
}
