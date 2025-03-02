package backend.academy.bot.routing;

import backend.academy.bot.BotKeyboards;
import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.rest.BotController;
import backend.academy.bot.telegram.utils.TelegramAPI;
import backend.academy.bot.telegram.utils.filters.UpdateProcessor;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.LinkUpdate;
import backend.academy.dto.ListLinkResponse;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.Keyboard;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import static backend.academy.bot.rest.BotController.getUpdateInfo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource("classpath:application-test.yaml")
class BotRouterTest {
    @Autowired
    @Qualifier("helpMessage")
    private String helpMessage;
    @MockitoBean
    private TelegramAPI telegramAPI;
    @MockitoBean
    private ScrapperClient scrapperClient;
    @Autowired
    private UpdateProcessor updateProcessor;
    @Autowired
    private BotController botController;

    private static ListLinkResponse getTestLinksData() {
        return new ListLinkResponse(List.of(
            new LinkResponse(
                1L, "https://google.com", List.of("tag1, tag2"), List.of("filter1:value1", "filter2:value2")),
            new LinkResponse(
                2L, "https://github.com", List.of("tag1, tag2"), List.of("filter1:value1", "filter2:value2")),
            new LinkResponse(
                3L, "https://stackoverflow.com", List.of("tag1, tag2"), List.of("filter1:value1", "filter2:value2")),
            new LinkResponse(
                4L, "https://stackoverflow.com", List.of("tag1, tag2"), List.of("filter1:value1", "filter2:value2")),
            new LinkResponse(
                5L, "https://stackoverflow.com", List.of("tag1, tag2"), List.of("filter1:value1", "filter2:value2"))
        ), 5);
    }

    @BeforeEach
    void setUp() {
        when(scrapperClient.getLinks(any())).thenReturn(Mono.just(getTestLinksData()));
        when(telegramAPI.sendMessageAsync(any(Message.class), anyString())).thenCallRealMethod();
        when(telegramAPI.sendMessagesAsync(any(), any())).thenCallRealMethod();
        when(telegramAPI.sendMessageAsync(any(Message.class), anyString(), any(Keyboard.class))).thenCallRealMethod();
        when(telegramAPI.sendMessageAsync(anyLong(), anyString())).thenCallRealMethod();
        when(scrapperClient.addLink(anyLong(), any())).thenReturn(Mono.empty());
        when(scrapperClient.registerChat(anyLong())).thenReturn(Mono.empty());
        when(scrapperClient.removeLink(anyLong(), anyString())).thenReturn(Mono.empty());
    }

    @Test
    void normalBotPipeline() {
        var start = mockMessageUpdate("/start", 1L);
        var help = mockMessageUpdate("/help", 1L);
        var list = mockMessageUpdate("/list", 1L);
        var track = mockMessageUpdate("/track", 1L);
        var link = mockMessageUpdate("https://google.com", 1L);
        var incorrectLink = mockMessageUpdate("htps://google.com", 1L);
        var tags = mockMessageUpdate("work, fun", 1L);
        var filters = mockMessageUpdate(BotKeyboards.SKIP_TEXT, 1L);
        var untrack = mockMessageUpdate("/untrack", 1L);
        var defaultHandler = mockMessageUpdate("some text", 1L);

        updateProcessor.consumeUpdate(start);
        updateProcessor.consumeUpdate(help);
        updateProcessor.consumeUpdate(list);
        updateProcessor.consumeUpdate(track);
        updateProcessor.consumeUpdate(incorrectLink);
        updateProcessor.consumeUpdate(link);
        updateProcessor.consumeUpdate(tags);
        updateProcessor.consumeUpdate(filters);
        updateProcessor.consumeUpdate(untrack);
        updateProcessor.consumeUpdate(link);
        updateProcessor.consumeUpdate(defaultHandler);

        verify(telegramAPI, times(1)).sendMessage(1L, "Hello! Use /track command to start");
        verify(telegramAPI, times(1)).sendMessage(1L, helpMessage);
        verify(telegramAPI, times(1)).sendMessage(1L, "Subscribed");
        verify(telegramAPI, times(1)).sendMessage(1L, "Unsubscribed");
        verify(telegramAPI, times(1)).sendMessage(1L, "Incorrect url, try again");
        verify(telegramAPI, times(1)).sendMessage(1L, "Your input is not supported. Try /help");
        verify(scrapperClient, times(1)).removeLink(1L, link.message().text());
        verify(scrapperClient, times(1)).registerChat(1L);
        verify(scrapperClient, times(1)).getLinks(1L);
        verify(scrapperClient, times(1)).addLink(eq(1L), any());
    }

    private Update mockMessageUpdate(String text, Long chatId) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.text()).thenReturn(text);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(update.message()).thenReturn(message);
        return update;
    }

    @Test
    void sendUpdate() {
        LinkUpdate update = new LinkUpdate(
            1L, "https://google.com", "description", List.of(1L, 2L)
        );

        botController.sendUpdates(update).block();

        verify(telegramAPI, times(1)).sendMessage(1L,
            getUpdateInfo(update));
        verify(telegramAPI, times(1)).sendMessage(2L,
            getUpdateInfo(update));
    }
}
