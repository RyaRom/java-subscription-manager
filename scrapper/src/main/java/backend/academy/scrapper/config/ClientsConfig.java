package backend.academy.scrapper.config;

import backend.academy.scrapper.clients.BotClient;
import backend.academy.scrapper.clients.BotHttpClient;
import backend.academy.scrapper.clients.BotKafkaClient;
import java.time.Duration;
import javax.naming.ConfigurationException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import static backend.academy.configuration.CustomHeaders.GITHUB_API_VERSION;

@Configuration
@RequiredArgsConstructor
public class ClientsConfig {
    private final ClientProps clientProps;

    @Bean
    public WebClient githubWebClient() {
        HttpClient httpClient = HttpClient.create()
            .responseTimeout(Duration.ofMillis(clientProps.timeout()));
        var builder = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .baseUrl(clientProps.githubUrl())
            .defaultHeader(GITHUB_API_VERSION, "2022-11-28")
            .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json");
        if (clientProps.githubToken() != null) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION,
                "Bearer " + clientProps.githubToken());
        }
        return builder.build();
    }

    @Bean
    public WebClient stackOverflowWebClient() {
        HttpClient httpClient = HttpClient.create()
            .responseTimeout(Duration.ofMillis(clientProps.timeout()));
        var builder = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .baseUrl(clientProps.stackOverflowUrl());
        return builder.build();
    }

    @Bean
    public WebClient botWebClient() {
        HttpClient httpClient = HttpClient.create()
            .responseTimeout(Duration.ofMillis(clientProps.timeout()));
        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .baseUrl(clientProps.botUrl())
            .defaultHeader("Content-Type", "application/json")
            .build();
    }

    @Bean
    public BotClient botClient(WebClient botWebClient, KafkaTemplate<Object, Object> kafkaTemplate)
        throws ConfigurationException {
        if (clientProps.clientType() == null ||
            clientProps.clientType().equalsIgnoreCase("http")) {
            return new BotHttpClient(botWebClient);
        } else if (clientProps.clientType().equalsIgnoreCase("kafka")) {
            return new BotKafkaClient(kafkaTemplate);
        } else {
            throw new ConfigurationException("Unknown client type " + clientProps.clientType());
        }
    }
}
