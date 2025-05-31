package backend.academy.bot.config;

import backend.academy.configuration.AppConfig;
import backend.academy.configuration.AspectModulesConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    AppConfig.class,
    ClientsConfig.class,
    TelegramConfig.class,
    RedisConfig.class,
    AspectModulesConfig.class,
})
public class BotModuleConfig {
}
