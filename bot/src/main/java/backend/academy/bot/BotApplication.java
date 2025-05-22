package backend.academy.bot;

import backend.academy.bot.config.BotClientsProps;
import backend.academy.bot.config.BotConfig;
import backend.academy.bot.config.DataProps;
import backend.academy.bot.config.TelegramConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@EnableWebFlux
@EnableConfigurationProperties({
    BotConfig.class,
    BotClientsProps.class,
    TelegramConfig.class,
    DataProps.class,
})
public class BotApplication {
    public static void main(String[] args) {
        SpringApplication.run(BotApplication.class, args);
    }
}
