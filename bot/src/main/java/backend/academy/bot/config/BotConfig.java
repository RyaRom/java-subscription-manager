package backend.academy.bot.config;

import backend.academy.bot.telegram.utils.filters.UpdateProcessor;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record BotConfig(@NotEmpty String telegramToken) {

    @Bean
    public TelegramBot telegramBot(UpdateProcessor updateProcessor) {
        var telegramBot = new TelegramBot(telegramToken);
        telegramBot.setUpdatesListener(updates -> {
                updates.forEach(updateProcessor::consumeUpdate);
                return UpdatesListener.CONFIRMED_UPDATES_ALL;
            },
            e -> {
                if (e.response() != null) {
                    e.response().errorCode();
                    e.response().description();
                } else {
                    e.printStackTrace();
                }
            });
        return telegramBot;
    }
}
