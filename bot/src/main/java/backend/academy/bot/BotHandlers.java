package backend.academy.bot;

import backend.academy.bot.telegram.utils.FilterRegister;
import backend.academy.bot.telegram.utils.TelegramAPI;
import backend.academy.bot.telegram.utils.annotations.FilterParam;
import backend.academy.bot.telegram.utils.annotations.Handler;
import backend.academy.bot.telegram.utils.annotations.MessageHandler;
import com.pengrad.telegrambot.model.Message;
import lombok.RequiredArgsConstructor;

@Handler
@RequiredArgsConstructor
public class BotHandlers {
    private final TelegramAPI telegramAPI;

    @MessageHandler(
        filters = {FilterRegister.CommandFilter.class},
        params = @FilterParam(key = "commands", value = "start")
    )
    public void start(Message message) {
        telegramAPI.sendMessage(message.chat().id(), "Registered user " + message.chat().id());
    }

    @MessageHandler(
        filters = {FilterRegister.CommandFilter.class},
        params = @FilterParam(key = "commands", value = "help")
    )
    public void help(Message message) {
        telegramAPI.sendMessage(message.chat().id(), "/help activated");
    }

    @MessageHandler(priority = 10)
    public void defaultHandler(Message message) {
        telegramAPI.sendMessage(message.chat().id(), "No other handlers were activated");
    }
}
