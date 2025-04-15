package backend.academy.bot.routing;

import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.config.BotConfig.BotCommands;
import backend.academy.bot.repository.UserDataCacheRepository;
import backend.academy.bot.telegram.sdk.annotations.FilterParam;
import backend.academy.bot.telegram.sdk.annotations.MessageHandler;
import backend.academy.bot.telegram.sdk.annotations.Router;
import backend.academy.bot.telegram.sdk.filters.FilterRegister;
import backend.academy.bot.telegram.sdk.utils.TelegramAPI;
import backend.academy.dto.LinkResponse;
import com.pengrad.telegrambot.model.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import static backend.academy.bot.telegram.sdk.filters.FilterParameter.COMMANDS;
import static backend.academy.bot.telegram.sdk.logging.MDCLogger.logOnError;
import static backend.academy.bot.telegram.sdk.logging.MDCLogger.logOnNext;

@Router
@Log4j2
@RequiredArgsConstructor
@SuppressWarnings("VA_FORMAT_STRING_USES_NEWLINE")
public class BotRouter {
    private final TelegramAPI telegramAPI;

    private final WebClient botHttpClient;

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
        //TODO log everywhere like here
        return Mono.just(message)
            .doOnEach(logOnNext(m -> log.info("In handler listLinks {}", message.chat().id())))
            .then(scrapperClient
                .getLinks(message.chat().id()))
            .flatMapMany(res -> Flux.fromIterable(res.links()))
            .flatMap(res -> telegramAPI.sendMessageAsync(message, getPrettyLinkInfo(res)))
            .then();
    }

    @MessageHandler(priority = 100, filters = FilterRegister.NotEmptyTextFilter.class)
    public Mono<Void> defaultHandler(Message message) {
        log.info("In handler default {}", message.chat().id());
        return telegramAPI.sendMessageAsync(message, "Your input is not supported. Try /help");
    }

    /**
     * Use only to set up commands from application.yaml
     */
    @MessageHandler(
        filters = {FilterRegister.CommandFilter.class},
        params = @FilterParam(key = COMMANDS, value = "setup"),
        priority = 0)
    public Mono<Void> setup(Message message) {
        return botHttpClient
            .post()
            .uri("/setMyCommands")
            .bodyValue(botCommands)
            .retrieve()
            .toBodilessEntity()
            .doOnEach(logOnNext(res -> log.info("Commands are set up")))
            .doOnEach(logOnError(err -> log.error("Failed to set up commands {}", err)))
            .then();
    }

    private String getPrettyLinkInfo(LinkResponse linkResponse) {
        return String.format(
            "url: %s\ntags: %s\nfilters: %s",
            linkResponse.url(), String.join(", ", linkResponse.tags()), String.join(", ", linkResponse.filters()));
    }
}
