package backend.academy.bot.telegram.utils;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TelegramAPI {

    @Lazy
    @Autowired
    //field injection to avoid circular dependency
    private TelegramBot telegramBot;

    public void sendMessage(Long chatId, String text) {
        SendMessage request = new SendMessage(chatId, text)
            .parseMode(ParseMode.HTML);
        telegramBot.execute(request);
    }

}
