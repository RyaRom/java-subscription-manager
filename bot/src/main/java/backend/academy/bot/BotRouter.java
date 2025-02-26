package backend.academy.bot;

import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.telegram.utils.TelegramAPI;
import backend.academy.bot.telegram.utils.annotations.FilterParam;
import backend.academy.bot.telegram.utils.annotations.MessageHandler;
import backend.academy.bot.telegram.utils.annotations.Router;
import backend.academy.bot.telegram.utils.filters.FilterParameter;
import backend.academy.bot.telegram.utils.filters.FilterRegister;
import backend.academy.bot.telegram.utils.fsm.FSMContext;
import com.pengrad.telegrambot.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import static backend.academy.bot.BotKeyboards.SKIP_TEXT;
import static backend.academy.bot.BotKeyboards.getSkipButton;
import static backend.academy.bot.telegram.utils.filters.FilterParameter.COMMANDS;

@Router
@Log4j2
@RequiredArgsConstructor
public class BotRouter {
    private final TelegramAPI telegramAPI;
    private final FSMContext fsmContext;
    @Qualifier("helpMessage")
    private final String helpMessage;
    private final ScrapperClient scrapperClient;


    @MessageHandler(
        filters = {FilterRegister.CommandFilter.class},
        params = @FilterParam(key = COMMANDS, value = "start")
    )
    public void start(Message message) {
        log.info("In handler start {}", message.chat().id());
        scrapperClient.registerChat(message.chat().id());
        telegramAPI.sendMessage(message, "Hello! Use /track command to start");
    }

    @MessageHandler(
        filters = {FilterRegister.CommandFilter.class},
        params = @FilterParam(key = COMMANDS, value = "help")
    )
    public void help(Message message) {
        log.info("In handler help {}", message.chat().id());
        telegramAPI.sendMessage(message, helpMessage);
    }

    @MessageHandler(priority = 100)
    public void defaultHandler(Message message) {
        log.info("In handler default {}", message.chat().id());
        telegramAPI.sendMessage(message, "Your input is not supported. Try /help");
    }

    @MessageHandler(
        filters = {FilterRegister.CommandFilter.class},
        params = @FilterParam(key = COMMANDS, value = "track"))
    public void track(Message message) {
        log.info("In handler track {}", message.chat().id());
        fsmContext.setState(message, SubscriptionBotState.WAITING_FOR_LINK);
        telegramAPI.sendMessage(message, "Send link to track");
    }

    @MessageHandler(
        filters = {FilterRegister.StateFilter.class, FilterRegister.UrlFilter.class},
        //weird compilation error with Enum.name()
        params = @FilterParam(key = FilterParameter.STATE, value = "WAITING_FOR_LINK")
    )
    public void parseLink(Message message) {
        log.info("In handler parseLink {}", message.chat().id());
        fsmContext.setState(message, SubscriptionBotState.WAITING_FOR_TAGS);
        telegramAPI.sendMessage(message, "Send personal tags for this link (optional)", getSkipButton());
    }

    @MessageHandler(
        filters = FilterRegister.StateFilter.class,
        params = @FilterParam(key = FilterParameter.STATE, value = "WAITING_FOR_LINK")
    )
    public void failedToParseLink(Message message) {
        log.info("In handler failedToParseLink {}", message.chat().id());
        telegramAPI.sendMessage(message, "Incorrect url, try again");
    }

    @MessageHandler(
        filters = FilterRegister.StateFilter.class,
        params = @FilterParam(key = FilterParameter.STATE, value = "WAITING_FOR_TAGS")
    )
    public void parseTags(Message message) {
        log.info("In handler parseTags {}", message.chat().id());
        if (!message.text().equals(SKIP_TEXT)) {
            //TODO: модель для временного сохранения обьекта пользователя в боте
        }
        telegramAPI.sendMessage(message, "Send personal tags for this link (optional)", getSkipButton());
    }
}
