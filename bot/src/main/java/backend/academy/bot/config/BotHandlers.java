package backend.academy.bot.config;

import backend.academy.bot.MessageHandler;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class BotHandlers {

    @Lazy
    @Autowired
    //field injection to avoid circular dependency
    private TelegramBot telegramBot;

    public void sendMessage(Long chatId, String text) {
        SendMessage request = new SendMessage(chatId, text)
            .parseMode(ParseMode.HTML);
        telegramBot.execute(request);
    }

    @Bean
    public MessageHandler messageHandler() {
        return message -> {
            if (message != null && message.text() != null) {
                sendMessage(message.chat().id(), message.text());
            }
        };
    }
}
