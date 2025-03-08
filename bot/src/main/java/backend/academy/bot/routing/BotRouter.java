package backend.academy.bot.routing;

import static backend.academy.bot.telegram.utils.filters.FilterParameter.COMMANDS;

import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.config.BotConfig.BotCommands;
import backend.academy.bot.repository.UserDataCacheRepository;
import backend.academy.bot.telegram.utils.TelegramAPI;
import backend.academy.bot.telegram.utils.annotations.FilterParam;
import backend.academy.bot.telegram.utils.annotations.MessageHandler;
import backend.academy.bot.telegram.utils.annotations.Router;
import backend.academy.bot.telegram.utils.filters.FilterRegister;
import backend.academy.dto.LinkResponse;
import com.pengrad.telegrambot.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Router
@Log4j2
@RequiredArgsConstructor
public class BotRouter {
    private final TelegramAPI telegramAPI;

    @Qualifier("tgHttpClient")
    private final WebClient tgHttpClient;

    @Qualifier("helpMessage")
    private final String helpMessage;

    private final BotCommands botCommands;

    private final ScrapperClient scrapperClient;
    private final UserDataCacheRepository userDataCacheRepository;

    @MessageHandler(
            filters = {FilterRegister.CommandFilter.class},
            params = @FilterParam(key = COMMANDS, value = "start"),
            priority = 0)
    public Mono<Void> start(Message message) {
        log.info("In handler start {}", message.chat().id());
        return scrapperClient
                .registerChat(message.chat().id())
                .then(userDataCacheRepository.clearUser(message.chat().id()))
                .then(telegramAPI.sendMessageAsync(message, "Hello! Use /track command to start"));
    }

    @MessageHandler(
            filters = {FilterRegister.CommandFilter.class},
            params = @FilterParam(key = COMMANDS, value = "help"),
            priority = 0)
    public Mono<Void> help(Message message) {
        log.info("In handler help {}", message.chat().id());
        return telegramAPI.sendMessageAsync(message, helpMessage);
    }

    @MessageHandler(
            filters = {FilterRegister.CommandFilter.class},
            params = @FilterParam(key = COMMANDS, value = "list"),
            priority = 0)
    public Mono<Void> listLinks(Message message) {
        log.info("In handler listLinks {}", message.chat().id());
        return scrapperClient
                .getLinks(message.chat().id())
                .flatMapMany(res -> Flux.fromIterable(res.links()))
                .flatMap(res -> telegramAPI.sendMessageAsync(message, getPrettyLinkInfo(res)))
                .then();
    }

    @MessageHandler(priority = 100, filters = FilterRegister.NotEmptyTextFilter.class)
    public Mono<Void> defaultHandler(Message message) {
        log.info("In handler default {}", message.chat().id());
        return telegramAPI.sendMessageAsync(message, "Your input is not supported. Try /help");
    }

    /** Use only to set up commands from application.yaml */
    @MessageHandler(
            filters = {FilterRegister.CommandFilter.class},
            params = @FilterParam(key = COMMANDS, value = "setup"),
            priority = 0)
    public Mono<Void> setup(Message message) {
        return tgHttpClient
                .post()
                .uri("/setMyCommands")
                .bodyValue(botCommands)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(res -> log.info("Commands are set up"))
                .doOnError(err -> log.error("Failed to set up commands", err))
                .then();
    }

    private String getPrettyLinkInfo(LinkResponse linkResponse) {
        return String.format(
                "url: %s%ntags: %s%nfilters: %s",
                linkResponse.url(), String.join(", ", linkResponse.tags()), String.join(", ", linkResponse.filters()));
    }
}
