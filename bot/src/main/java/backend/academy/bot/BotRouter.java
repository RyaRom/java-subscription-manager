package backend.academy.bot;

import backend.academy.bot.repository.SubscriptionBotState;
import backend.academy.bot.telegram.utils.fsm.FSMContext;
import backend.academy.bot.telegram.utils.filters.FilterParameter;
import backend.academy.bot.telegram.utils.filters.FilterRegister;
import backend.academy.bot.telegram.utils.TelegramAPI;
import backend.academy.bot.telegram.utils.annotations.FilterParam;
import backend.academy.bot.telegram.utils.annotations.MessageHandler;
import backend.academy.bot.telegram.utils.annotations.Router;
import com.pengrad.telegrambot.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Router
@Log4j2
@RequiredArgsConstructor
public class BotRouter {
    private final TelegramAPI telegramAPI;
    private final FSMContext fsmContext;

    @MessageHandler(
        filters = {FilterRegister.CommandFilter.class},
        params = @FilterParam(key = FilterParameter.COMMANDS, value = "start")
    )
    public void start(Message message) {
        log.info("In handler start {}", message.chat().id());
        telegramAPI.sendMessage(message, "Registered user " + message.chat().id());
        fsmContext.setState(message, SubscriptionBotState.REGISTRATION);
    }

    @MessageHandler(
        filters = {FilterRegister.CommandFilter.class},
        params = @FilterParam(key = FilterParameter.COMMANDS, value = "help")
    )
    public void help(Message message) {
        log.info("In handler help {}", message.chat().id());
        telegramAPI.sendMessage(message, "/help activated");
    }

    @MessageHandler(priority = 10)
    public void defaultHandler(Message message) {
        log.info("In handler default {}", message.chat().id());
        telegramAPI.sendMessage(message, "default");
    }

    @MessageHandler(
        filters = FilterRegister.StateFilter.class,
        //weird compilation error with REGISTRATION.name()
        params = @FilterParam(key = FilterParameter.STATE, value = "REGISTRATION")
    )
    public void registration(Message message) {
        log.info("In handler registration {}", message.chat().id());
        telegramAPI.sendMessage(message, "registration is on");
    }
}
