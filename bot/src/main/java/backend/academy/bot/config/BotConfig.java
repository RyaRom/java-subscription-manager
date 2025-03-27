package backend.academy.bot.config;

import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;

@Validated
@Log4j2
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record BotConfig(BotCommands settings) {

    @Bean
    public String helpMessage() {
        return settings.commands.stream().map(BotCommand::toString).collect(Collectors.joining("%n"));
    }

    @Bean
    public BotCommands botCommands() {
        return settings;
    }

    public record BotCommands(List<BotCommand> commands) {
    }

    public record BotCommand(String command, String description) {
        @Override
        public String toString() {
            return command + " - " + description;
        }
    }
}
