package backend.academy.bot.config;

import backend.academy.bot.telegram.sdk.BotContext;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;

@Validated
@Log4j2
@ConfigurationProperties(prefix = "app.telegram", ignoreUnknownFields = false)
public record TelegramConfig(@NotEmpty String telegramToken) {

    @Bean
    public String telegramToken() {
        return telegramToken;
    }

    @Bean
    public TelegramBot telegramBot(BotContext botContext) {
        var telegramBot = new TelegramBot(telegramToken);
        telegramBot.setUpdatesListener(
                updates -> {
                    updates.forEach(botContext::emmitUpdate);
                    return UpdatesListener.CONFIRMED_UPDATES_ALL;
                },
                e -> log.error("ERROR IN TELEGRAM {}", e.response().description()));
        return telegramBot;
    }
}
