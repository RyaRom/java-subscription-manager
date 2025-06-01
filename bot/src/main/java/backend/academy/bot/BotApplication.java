package backend.academy.bot;

import backend.academy.bot.config.BotProps;
import backend.academy.bot.config.ClientsProps;
import backend.academy.bot.config.DataProps;
import backend.academy.bot.config.TelegramProps;
import backend.academy.configuration.ResilienceProps;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableWebFlux
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableConfigurationProperties({
    BotProps.class,
    ClientsProps.class,
    TelegramProps.class,
    DataProps.class,
    ResilienceProps.class
})
public class BotApplication {
    public static void main(String[] args) {
        SpringApplication.run(BotApplication.class, args);
    }
}
