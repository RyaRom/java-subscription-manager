package backend.academy.bot.config;

import backend.academy.bot.telegram.sdk.BotContext;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
@Log4j2
public class TelegramConfig {
    private final TelegramProps telegramProps;

    @Bean
    public TelegramBot telegramBot(BotContext botContext) {
        var telegramBot = new TelegramBot(telegramProps.telegramToken());
        telegramBot.setUpdatesListener(
            updates -> {
                updates.forEach(botContext::emmitUpdate);
                return UpdatesListener.CONFIRMED_UPDATES_ALL;
            },
            e -> log.error("ERROR IN TELEGRAM {}", e.response().description()));
        return telegramBot;
    }
}
