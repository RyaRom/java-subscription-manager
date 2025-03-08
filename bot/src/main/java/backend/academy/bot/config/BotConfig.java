package backend.academy.bot.config;

import backend.academy.bot.telegram.utils.filters.UpdateProcessor;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.reactive.function.client.WebClient;

@Validated
@Log4j2
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record BotConfig(@NotEmpty String telegramToken, @NotEmpty String scrapperUrl, BotCommands settings) {

    @Bean
    @Qualifier("helpMessage")
    public String helpMessage() {
        return settings.commands.stream().map(BotCommand::toString).collect(Collectors.joining("%n"));
    }

    @Bean
    public BotCommands botCommands() {
        return settings;
    }

    @Bean
    @Qualifier("scrapperHttpClient")
    public WebClient scrapperHttpClient() {
        return WebClient.builder()
                .baseUrl(scrapperUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    @Qualifier("tgHttpClient")
    public WebClient botHttpClient() {
        return WebClient.builder()
                .baseUrl("https://api.telegram.org/bot" + telegramToken)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public TelegramBot telegramBot(UpdateProcessor updateProcessor) {
        var telegramBot = new TelegramBot(telegramToken);
        telegramBot.setUpdatesListener(
                updates -> {
                    updates.forEach(updateProcessor::consumeUpdate);
                    return UpdatesListener.CONFIRMED_UPDATES_ALL;
                },
                e -> {
                    if (e.response() != null) {
                        log.error(e.response().errorCode());
                        log.error(e.response().description());
                    } else {
                        e.printStackTrace();
                    }
                });
        return telegramBot;
    }

    public record BotCommands(List<BotCommand> commands) {}

    public record BotCommand(String command, String description) {
        @Override
        public String toString() {
            return command + " - " + description;
        }
    }
}
