package backend.academy.bot.config;

import backend.academy.bot.MessageHandler;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record BotConfig(@NotEmpty String telegramToken) {

    @Bean
    public TelegramBot telegramBot(MessageHandler messageHandler) {
        var telegramBot = new TelegramBot(telegramToken);
        telegramBot.setUpdatesListener(updates -> {
                updates.forEach(update -> {
                    Message message = update.message();
                    messageHandler.handle(message);
                });
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
