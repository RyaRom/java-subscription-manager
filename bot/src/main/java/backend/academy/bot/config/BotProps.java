package backend.academy.bot.config;

import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@Log4j2
@ConfigurationProperties(prefix = "app.bot", ignoreUnknownFields = false)
public record BotProps(BotCommands settings) {
    public String helpMessage() {
        return settings.commands().stream()
                .map(BotCommand::toString)
                .reduce((a, b) -> a + "\n" + b)
                .orElse("No commands found");
    }

    public record BotCommands(List<BotCommand> commands) {}

    public record BotCommand(String command, String description) {
        @Override
        public String toString() {
            return command + " - " + description;
        }
    }
}
