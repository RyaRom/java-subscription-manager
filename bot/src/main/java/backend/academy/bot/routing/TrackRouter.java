package backend.academy.bot.routing;

import backend.academy.bot.SubscriptionBotState;
import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.repository.UserDataCacheRepository;
import backend.academy.bot.rest.dto.AddLinkRequest;
import backend.academy.bot.telegram.utils.TelegramAPI;
import backend.academy.bot.telegram.utils.annotations.FilterParam;
import backend.academy.bot.telegram.utils.annotations.MessageHandler;
import backend.academy.bot.telegram.utils.annotations.Router;
import backend.academy.bot.telegram.utils.filters.FilterParameter;
import backend.academy.bot.telegram.utils.filters.FilterRegister;
import backend.academy.bot.telegram.utils.fsm.FSMContext;
import com.pengrad.telegrambot.model.Message;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import static backend.academy.bot.BotKeyboards.SKIP_TEXT;
import static backend.academy.bot.BotKeyboards.getSkipButton;
import static backend.academy.bot.telegram.utils.filters.FilterParameter.COMMANDS;

@Router
@Log4j2
@RequiredArgsConstructor
public class TrackRouter {
    private final TelegramAPI telegramAPI;
    private final FSMContext fsmContext;
    private final UserDataCacheRepository userDataCacheRepository;
    private final ScrapperClient scrapperClient;

    @MessageHandler(
        filters = {FilterRegister.CommandFilter.class},
        params = @FilterParam(key = COMMANDS, value = "track"),
        priority = 0
    )
    public void track(Message message) {
        log.info("In handler track {}", message.chat().id());
        userDataCacheRepository.clearUser(message.chat().id());
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
        userDataCacheRepository.updateLink(message.chat().id(), message.text());
        fsmContext.setState(message, SubscriptionBotState.WAITING_FOR_TAGS);
        telegramAPI.sendMessage(message, "Send personal tags for this link (optional)", getSkipButton());
    }

    @MessageHandler(
        filters = FilterRegister.StateFilter.class,
        params = @FilterParam(key = FilterParameter.STATE, value = "WAITING_FOR_LINK"),
        priority = 10
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
            userDataCacheRepository.updateTags(message.chat().id(), message.text());
        }
        fsmContext.setState(message, SubscriptionBotState.WAITING_FOR_FILTERS);
        telegramAPI.sendMessage(message, "Send personal filters for this link (optional)", getSkipButton());
    }

    @MessageHandler(
        filters = FilterRegister.StateFilter.class,
        params = @FilterParam(key = FilterParameter.STATE, value = "WAITING_FOR_FILTERS")
    )
    public void parseFilters(Message message) {
        log.info("In handler parseFilters {}", message.chat().id());
        if (!message.text().equals(SKIP_TEXT)) {
            userDataCacheRepository.updateFilters(message.chat().id(), message.text());
        }
        var data = userDataCacheRepository.getUser(message.chat().id());
        scrapperClient.addLink(
            message.chat().id(),
            AddLinkRequest.builder()
                .link(data.link())
                .filters(List.of(data.filters().split(" ")))
                .tags(List.of(data.tags().split(" ")))
                .build()
        ).subscribe();
        userDataCacheRepository.clearUser(message.chat().id());
        telegramAPI.sendMessage(message, "Subscribed");
    }

    @MessageHandler(
        filters = {FilterRegister.CommandFilter.class},
        params = @FilterParam(key = COMMANDS, value = "untrack"),
        priority = 0
    )
    public void untrack(Message message) {
        log.info("In handler unsubscribe {}", message.chat().id());
        telegramAPI.sendMessage(message, "Enter the link. Your current links: /list");
        fsmContext.setState(message, SubscriptionBotState.WAITING_FOR_LINK_UNSUBSCRIBE);
    }

    @MessageHandler(
        filters = {FilterRegister.StateFilter.class, FilterRegister.UrlFilter.class},
        params = @FilterParam(key = FilterParameter.STATE, value = "WAITING_FOR_LINK_UNSUBSCRIBE")
    )
    public void unsubscribeParseLink(Message message) {
        log.info("In handler unsubscribeParseLink {}", message.chat().id());
        scrapperClient.removeLink(message.chat().id(), message.text())
            .subscribe();
        userDataCacheRepository.clearUser(message.chat().id());
        telegramAPI.sendMessage(message, "Unsubscribed");
    }

    @MessageHandler(
        filters = FilterRegister.StateFilter.class,
        params = @FilterParam(key = FilterParameter.STATE, value = "WAITING_FOR_LINK_UNSUBSCRIBE"),
        priority = 10
    )
    public void unsubscribeFailedToParseLink(Message message) {
        log.info("In handler unsubscribeFailedToParseLink {}", message.chat().id());
        telegramAPI.sendMessage(message, "Incorrect url, try again");
    }
}
