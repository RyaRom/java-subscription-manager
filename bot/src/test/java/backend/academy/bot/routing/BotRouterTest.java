package backend.academy.bot.routing;

import static backend.academy.bot.telegram.sdk.utils.TelegramAPI.getUpdateInfo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.bot.BaseIntegrationTest;
import backend.academy.bot.BotKeyboards;
import backend.academy.bot.clients.ScrapperHttpClient;
import backend.academy.bot.config.BotProps;
import backend.academy.bot.rest.BotController;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.LinkUpdate;
import backend.academy.dto.ListLinkResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;

class BotRouterTest extends BaseIntegrationTest {
    @Autowired
    private BotProps botProps;

    @MockitoBean
    private ScrapperHttpClient scrapperHttpClient;

    @Autowired
    private BotController botController;

    private static ListLinkResponse getTestLinksData() {
        return new ListLinkResponse(
                List.of(
                        new LinkResponse(
                                1L,
                                "https://google.com",
                                List.of("tag1, tag2"),
                                List.of("filter1:value1", "filter2:value2")),
                        new LinkResponse(
                                2L,
                                "https://github.com",
                                List.of("tag1, tag2"),
                                List.of("filter1:value1", "filter2:value2")),
                        new LinkResponse(
                                3L,
                                "https://stackoverflow.com",
                                List.of("tag1, tag2"),
                                List.of("filter1:value1", "filter2:value2")),
                        new LinkResponse(
                                4L,
                                "https://stackoverflow.com",
                                List.of("tag1, tag2"),
                                List.of("filter1:value1", "filter2:value2")),
                        new LinkResponse(
                                5L,
                                "https://stackoverflow.com",
                                List.of("tag1, tag2"),
                                List.of("filter1:value1", "filter2:value2"))),
                5);
    }

    @BeforeEach
    void setUp() {
        when(scrapperHttpClient.getLinks(any())).thenReturn(Mono.just(getTestLinksData()));
        when(scrapperHttpClient.addLink(anyLong(), any())).thenReturn(Mono.empty());
        when(scrapperHttpClient.registerChat(anyLong())).thenReturn(Mono.empty());
        when(scrapperHttpClient.removeLink(anyLong(), anyString())).thenReturn(Mono.empty());
    }

    @Test
    void normalBotPipeline() throws InterruptedException {
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

        botContext.emmitUpdate(start);
        botContext.emmitUpdate(help);
        botContext.emmitUpdate(list);
        botContext.emmitUpdate(track);
        botContext.emmitUpdate(incorrectLink);
        botContext.emmitUpdate(link);
        botContext.emmitUpdate(tags);
        botContext.emmitUpdate(filters);
        botContext.emmitUpdate(untrack);
        botContext.emmitUpdate(link);
        botContext.emmitUpdate(defaultHandler);

        Thread.sleep(2000);

        verify(telegramAPI, times(1)).sendMessage(1L, "Hello! Use /track command to start");
        verify(telegramAPI, times(1)).sendMessage(1L, botProps.helpMessage());
        verify(telegramAPI, times(1)).sendMessage(1L, "Subscribed");
        verify(telegramAPI, times(1)).sendMessage(1L, "Unsubscribed");
        verify(telegramAPI, times(1)).sendMessage(1L, "Incorrect url, try again");
        verify(telegramAPI, times(1)).sendMessage(1L, "Your input is not supported. Try /help");
        verify(scrapperHttpClient, times(1)).removeLink(1L, link.message().text());
        verify(scrapperHttpClient, times(1)).registerChat(1L);
        verify(scrapperHttpClient, times(1)).getLinks(1L);
        verify(scrapperHttpClient, times(1)).addLink(eq(1L), any());
    }

    @Test
    void sendUpdate() {
        LinkUpdate update = new LinkUpdate(1L, "https://google.com", "description", List.of(1L, 2L));

        botController.sendUpdates(update).block();

        verify(telegramAPI, times(1)).sendMessage(1L, getUpdateInfo(update));
        verify(telegramAPI, times(1)).sendMessage(2L, getUpdateInfo(update));
    }
}
