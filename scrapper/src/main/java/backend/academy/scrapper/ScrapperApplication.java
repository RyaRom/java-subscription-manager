package backend.academy.scrapper;

import backend.academy.configuration.ResilienceProps;
import backend.academy.scrapper.config.ClientsProps;
import backend.academy.scrapper.config.DataConnectionProperties;
import backend.academy.scrapper.config.KafkaConfig;
import backend.academy.scrapper.config.ScrapperConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableWebFlux
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableConfigurationProperties({
    ScrapperConfig.class,
    ClientsProps.class,
    DataConnectionProperties.class,
    KafkaConfig.class,
    ResilienceProps.class,
})
public class ScrapperApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScrapperApplication.class, args);
    }
}
