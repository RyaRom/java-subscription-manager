package backend.academy.scrapper;

import backend.academy.scrapper.config.DatabaseConfig;
import backend.academy.scrapper.config.ClientsConfig;
import backend.academy.scrapper.config.KafkaConfig;
import backend.academy.scrapper.config.ScrapperConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableWebFlux
@EnableScheduling
@EnableConfigurationProperties({ScrapperConfig.class, ClientsConfig.class, DatabaseConfig.class, KafkaConfig.class})
public class ScrapperApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScrapperApplication.class, args);
    }
}
