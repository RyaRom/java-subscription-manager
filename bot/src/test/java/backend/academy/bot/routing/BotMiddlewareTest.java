package backend.academy.bot.routing;

import static org.mockito.Mockito.*;

import backend.academy.bot.BaseIntegrationTest;
import backend.academy.bot.clients.ScrapperClient;
import backend.academy.exception.BadLinkException;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

public class BotMiddlewareTest extends BaseIntegrationTest {
    @MockitoBean
    private ScrapperClient scrapperClient;

    @BeforeEach
    void setUp() throws URISyntaxException {
        when(scrapperClient.registerChat(anyLong()))
                .thenReturn(Mono.error(new WebClientRequestException(
                        new Throwable(), HttpMethod.DELETE, new URI("127.0.0.1"), HttpHeaders.EMPTY)));
        when(scrapperClient.getLinks(anyLong())).thenReturn(Mono.error(new BadLinkException("")));
    }

    @Test
    void assertErrorHandles() {
        var messageTest = mockMessageUpdate("/start", 1L);
        var messageTest2 = mockMessageUpdate("/list", 1L);

        botContext.consumeUpdate(messageTest);
        botContext.consumeUpdate(messageTest2);

        verify(telegramAPI, times(1)).sendMessage(1L, "Can't connect to internal server");
        verify(telegramAPI, times(1))
                .sendMessage(1L, "Link is not supported incorrect or duplicated. Your links: /list");
    }
}
