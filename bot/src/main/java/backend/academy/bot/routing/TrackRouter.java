package backend.academy.bot.routing;

import static backend.academy.bot.BotKeyboards.SKIP_TEXT;
import static backend.academy.bot.BotKeyboards.getSkipButton;
import static backend.academy.bot.telegram.sdk.filters.FilterParameter.COMMANDS;

import backend.academy.bot.SubscriptionBotState;
import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.clients.ScrapperPublisher;
import backend.academy.bot.repository.UserDataCacheRepository;
import backend.academy.bot.telegram.sdk.annotations.FilterParam;
import backend.academy.bot.telegram.sdk.annotations.MessageHandler;
import backend.academy.bot.telegram.sdk.annotations.Router;
import backend.academy.bot.telegram.sdk.filters.FilterParameter;
import backend.academy.bot.telegram.sdk.filters.FilterRegister;
import backend.academy.bot.telegram.sdk.fsm.FSMContext;
import backend.academy.bot.telegram.sdk.utils.TelegramAPI;
import backend.academy.dto.AddLinkRequest;
import com.pengrad.telegrambot.model.Message;
import java.util.Locale;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Router
@Log4j2
@RequiredArgsConstructor
public class TrackRouter {
    private final TelegramAPI telegramAPI;
    private final FSMContext fsmContext;
    private final UserDataCacheRepository userDataCacheRepository;
    private final ScrapperClient scrapperClient;
    private final ScrapperPublisher scrapperPublisher;

    @MessageHandler(
            filters = {FilterRegister.CommandFilter.class},
            params = @FilterParam(key = COMMANDS, value = "track"),
            priority = 0)
    public Mono<Void> track(Message message) {
        log.info("In handler track {}", message.chat().id());
        return userDataCacheRepository
                .clearUser(message.chat().id())
                .then(fsmContext.setState(message, SubscriptionBotState.WAITING_FOR_LINK))
                .then(telegramAPI.sendMessageAsync(message, "Send link to track"));
    }

    @MessageHandler(
            filters = {FilterRegister.StateFilter.class, FilterRegister.UrlFilter.class},
            // weird compilation error with Enum.name()
            params = @FilterParam(key = FilterParameter.STATE, value = "WAITING_FOR_LINK"))
    public Mono<Void> parseLink(Message message) {
        log.info("In handler parseLink {}", message.chat().id());
        return userDataCacheRepository
                .updateLink(message.chat().id(), message.text())
                .then(fsmContext.setState(message, SubscriptionBotState.WAITING_FOR_TAGS))
                .then(telegramAPI.sendMessageAsync(
                        message, "Send personal tags for this link (optional)", getSkipButton()));
    }

    @MessageHandler(
            filters = FilterRegister.StateFilter.class,
            params = @FilterParam(key = FilterParameter.STATE, value = "WAITING_FOR_LINK"),
            priority = 10)
    public Mono<Void> failedToParseLink(Message message) {
        log.info("In handler failedToParseLink {}", message.chat().id());
        return telegramAPI.sendMessageAsync(message, "Incorrect url, try again");
    }

    @MessageHandler(
            filters = FilterRegister.StateFilter.class,
            params = @FilterParam(key = FilterParameter.STATE, value = "WAITING_FOR_TAGS"))
    public Mono<Void> parseTags(Message message) {
        log.info("In handler parseTags {}", message.chat().id());
        Mono<Void> result = Mono.empty();
        if (!message.text().equals(SKIP_TEXT)) {
            result = result.then(
                    userDataCacheRepository.updateTags(message.chat().id(), message.text()));
        }
        return result.then(fsmContext.setState(message, SubscriptionBotState.WAITING_FOR_FILTERS))
                .then(telegramAPI.sendMessageAsync(
                        message, "Send personal filters for this link (optional)", getSkipButton()));
    }

    @MessageHandler(
            filters = FilterRegister.StateFilter.class,
            params = @FilterParam(key = FilterParameter.STATE, value = "WAITING_FOR_FILTERS"))
    public Mono<Void> parseFilters(Message message) {
        log.info("In handler parseFilters {}", message.chat().id());
        Mono<Void> result = Mono.empty();
        if (!message.text().equals(SKIP_TEXT)) {
            result = result.then(
                    userDataCacheRepository.updateFilters(message.chat().id(), message.text()));
        }
        return result.then(Mono.defer(
                        () -> userDataCacheRepository.getUser(message.chat().id())))
                .flatMap(data -> scrapperPublisher.addLink(
                        message.chat().id(),
                        AddLinkRequest.builder()
                                .link(data.getLink().toLowerCase(Locale.ROOT))
                                .filters(Stream.of(data.getFilters().split(" "))
                                        .filter(s -> !s.isEmpty())
                                        .toList())
                                .tags(Stream.of(data.getTags().split(" "))
                                        .filter(s -> !s.isEmpty())
                                        .toList())
                                .build()))
                .then(userDataCacheRepository.clearUser(message.chat().id()))
                .then(telegramAPI.sendMessageAsync(message, "Subscribed"));
    }

    @MessageHandler(
            filters = {FilterRegister.CommandFilter.class},
            params = @FilterParam(key = COMMANDS, value = "untrack"),
            priority = 0)
    public Mono<Void> untrack(Message message) {
        log.info("In handler unsubscribe {}", message.chat().id());
        return telegramAPI
                .sendMessageAsync(message, "Enter the link. Your current links: /list")
                .then(fsmContext.setState(message, SubscriptionBotState.WAITING_FOR_LINK_UNSUBSCRIBE));
    }

    @MessageHandler(
            filters = {FilterRegister.StateFilter.class, FilterRegister.UrlFilter.class},
            params = @FilterParam(key = FilterParameter.STATE, value = "WAITING_FOR_LINK_UNSUBSCRIBE"))
    public Mono<Void> unsubscribeParseLink(Message message) {
        log.info("In handler unsubscribeParseLink {}", message.chat().id());
        return scrapperPublisher
                .removeLink(message.chat().id(), message.text())
                .then(userDataCacheRepository.clearUser(message.chat().id()))
                .then(telegramAPI.sendMessageAsync(message, "Unsubscribed"));
    }

    @MessageHandler(
            filters = FilterRegister.StateFilter.class,
            params = @FilterParam(key = FilterParameter.STATE, value = "WAITING_FOR_LINK_UNSUBSCRIBE"),
            priority = 10)
    public Mono<Void> unsubscribeFailedToParseLink(Message message) {
        log.info("In handler unsubscribeFailedToParseLink {}", message.chat().id());
        return telegramAPI.sendMessageAsync(message, "Incorrect url, try again");
    }
}
