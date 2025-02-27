package backend.academy.bot.routing;

import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.repository.UserDataCacheRepository;
import backend.academy.bot.rest.dto.LinkResponse;
import backend.academy.bot.telegram.utils.TelegramAPI;
import backend.academy.bot.telegram.utils.annotations.FilterParam;
import backend.academy.bot.telegram.utils.annotations.MessageHandler;
import backend.academy.bot.telegram.utils.annotations.Router;
import backend.academy.bot.telegram.utils.filters.FilterRegister;
import com.pengrad.telegrambot.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import reactor.core.publisher.Flux;
import static backend.academy.bot.telegram.utils.filters.FilterParameter.COMMANDS;

@Router
@Log4j2
@RequiredArgsConstructor
public class BotRouter {
    private final TelegramAPI telegramAPI;
    @Qualifier("helpMessage")
    private final String helpMessage;
    private final ScrapperClient scrapperClient;
    private final UserDataCacheRepository userDataCacheRepository;

    @MessageHandler(
        filters = {FilterRegister.CommandFilter.class},
        params = @FilterParam(key = COMMANDS, value = "start"),
        priority = 0
    )
    public void start(Message message) {
        log.info("In handler start {}", message.chat().id());
        scrapperClient.registerChat(message.chat().id())
            .subscribe();
        userDataCacheRepository.clearUser(message.chat().id());
        telegramAPI.sendMessage(message, "Hello! Use /track command to start");
    }

    @MessageHandler(
        filters = {FilterRegister.CommandFilter.class},
        params = @FilterParam(key = COMMANDS, value = "help"),
        priority = 0
    )
    public void help(Message message) {
        log.info("In handler help {}", message.chat().id());
        telegramAPI.sendMessage(message, helpMessage);
    }

    @MessageHandler(
        filters = {FilterRegister.CommandFilter.class},
        params = @FilterParam(key = COMMANDS, value = "list"),
        priority = 0
    )
    public void listLinks(Message message) {
        log.info("In handler listLinks {}", message.chat().id());
        scrapperClient.getLinks(message.chat().id())
            .flatMapMany(res -> Flux.fromIterable(res.links()))
            .doOnNext(res -> telegramAPI.sendMessage(message, getPrettyLinkInfo(res)))
            .subscribe();
    }

    @MessageHandler(priority = 100)
    public void defaultHandler(Message message) {
        log.info("In handler default {}", message.chat().id());
        telegramAPI.sendMessage(message, "Your input is not supported. Try /help");
    }

    private String getPrettyLinkInfo(LinkResponse linkResponse) {
        return String.format(
            "url: %s\ntags: %s\nfilters: %s",
            linkResponse.url(),
            String.join(", ", linkResponse.tags()),
            String.join(", ", linkResponse.filters())
        );
    }
}
