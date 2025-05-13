package backend.academy.bot;

import backend.academy.bot.telegram.sdk.BotContext;
import backend.academy.bot.telegram.sdk.utils.TelegramAPI;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.Keyboard;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
public class BaseIntegrationTest {
    @Autowired
    protected BotContext botContext;

    @MockitoBean
    protected TelegramAPI telegramAPI;

    @BeforeEach
    void setUp() {
        when(telegramAPI.sendMessageAsync(any(Message.class), anyString())).thenCallRealMethod();
        when(telegramAPI.sendMessagesAsync(any(), any())).thenCallRealMethod();
        when(telegramAPI.sendMessageAsync(any(Message.class), anyString(), any(Keyboard.class)))
            .thenCallRealMethod();
        when(telegramAPI.sendMessageAsync(anyLong(), anyString())).thenCallRealMethod();
        when(telegramAPI.sendMessageAsyncWithRetry(anyLong(), anyString(), anyInt())).thenCallRealMethod();
    }

    protected Update mockMessageUpdate(String text, Long chatId) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        when(message.text()).thenReturn(text);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(update.message()).thenReturn(message);
        return update;
    }
}
