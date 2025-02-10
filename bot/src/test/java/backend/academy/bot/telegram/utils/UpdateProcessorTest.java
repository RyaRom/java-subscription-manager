package backend.academy.bot.telegram.utils;

import backend.academy.bot.telegram.utils.annotations.FilterParam;
import backend.academy.bot.telegram.utils.annotations.Handler;
import backend.academy.bot.telegram.utils.annotations.MessageHandler;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class UpdateProcessorTest {
    @Mock
    TelegramAPI telegramAPI;
    @Mock
    ApplicationContext applicationContext;
    @InjectMocks
    UpdateProcessor updateProcessor;

    @BeforeEach
    void setUp() {
        when(applicationContext.getBeanNamesForAnnotation(Handler.class))
            .thenReturn(new String[]{"TestHandlers1", "TestHandlers2"});
        when(applicationContext.getBean("TestHandlers1")).thenReturn(new TestHandlers1());
        when(applicationContext.getBean("TestHandlers2")).thenReturn(new TestHandlers2());

        updateProcessor.init();
    }

    private Update mockMessage(String text) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        when(message.text()).thenReturn(text);
        when(update.message()).thenReturn(message);
        return update;
    }

    @Test
    void noCommands() {
        List<Update> input = List.of(
            mockMessage(""),
            mockMessage("nothing"),
            mockMessage("     ")
        );

        input.forEach(updateProcessor::consumeUpdate);

        Mockito.verify(telegramAPI, Mockito.times(3)).sendMessage(1L, "defaultHandler");
    }

    @Test
    void commands() {
        List<Update> input = List.of(
            mockMessage("/cmd1"),
            mockMessage("/cmd2"),
            mockMessage("/cmd3")
        );

        input.forEach(updateProcessor::consumeUpdate);

        Mockito.verify(telegramAPI, Mockito.times(2)).sendMessage(1L, "allCommandsEverytime");
        Mockito.verify(telegramAPI, Mockito.times(0)).sendMessage(1L, "cmd2");
        Mockito.verify(telegramAPI, Mockito.times(2)).sendMessage(1L, "allCommandsFinal");
        Mockito.verify(telegramAPI, Mockito.times(1)).sendMessage(1L, "defaultHandler");
    }

    private class TestHandlers1 {
        @MessageHandler(
            filters = {FilterRegister.CommandFilter.class},
            params = @FilterParam(key = "commands", value = {"/cmd1", "cmd2"}),
            isFinal = false)
        public void allCommandsEverytime(Message message) {
            telegramAPI.sendMessage(1L, "allCommandsEverytime");
        }

        @MessageHandler(
            filters = {FilterRegister.CommandFilter.class},
            params = @FilterParam(key = "commands", value = {"cmd2"}),
            priority = 6,
            isFinal = false)
        public void cmd2After(Message message) {
            telegramAPI.sendMessage(1L, "cmd2");
        }
    }

    private class TestHandlers2 {
        @MessageHandler(
            filters = {FilterRegister.CommandFilter.class},
            priority = 5,
            params = @FilterParam(key = "commands", value = {"cmd1", "cmd2"}))
        public void allCommandsFinal(Message message) {
            telegramAPI.sendMessage(1L, "allCommandsFinal");
        }

        @MessageHandler(priority = 10)
        public void defaultHandler(Message message) {
            telegramAPI.sendMessage(1L, "defaultHandler");
        }
    }
}
