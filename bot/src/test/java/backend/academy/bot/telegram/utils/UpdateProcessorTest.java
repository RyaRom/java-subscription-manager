package backend.academy.bot.telegram.utils;

import backend.academy.bot.telegram.utils.annotations.FilterParam;
import backend.academy.bot.telegram.utils.annotations.MessageHandler;
import backend.academy.bot.telegram.utils.annotations.Router;
import backend.academy.bot.telegram.utils.filters.FilterParameter;
import backend.academy.bot.telegram.utils.filters.FilterRegister;
import backend.academy.bot.telegram.utils.filters.UpdateProcessor;
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
import static java.lang.Thread.sleep;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"DirectInvocationOnMock", "UnusedMethod"})
class UpdateProcessorTest {
    @Mock
    private TelegramAPI telegramAPI;
    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private FilterRegister filterRegister;
    @InjectMocks
    private UpdateProcessor updateProcessor;

    @BeforeEach
    void setUp() {
        when(applicationContext.getBeanNamesForAnnotation(Router.class))
            .thenReturn(new String[]{"TestHandlers1", "TestHandlers2"});
        when(applicationContext.getBean("TestHandlers1")).thenReturn(new TestHandlers1());
        when(applicationContext.getBean("TestHandlers2")).thenReturn(new TestHandlers2());
        when(filterRegister.getFilterInstance(FilterRegister.CommandFilter.class)).thenReturn(new FilterRegister.CommandFilter());

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
    void noCommands() throws InterruptedException {
        List<Update> input = List.of(
            mockMessage("a"),
            mockMessage("nothing"),
            mockMessage("     ")
        );

        input.forEach(updateProcessor::consumeUpdate);
        sleep(1000L);

        Mockito.verify(telegramAPI, Mockito.times(3)).sendMessage(1L, "defaultHandler");
    }

    @Test
    void commands() throws InterruptedException {
        List<Update> input = List.of(
            mockMessage("/cmd1"),
            mockMessage("/cmd2"),
            mockMessage("/cmd3")
        );

        input.forEach(updateProcessor::consumeUpdate);
        sleep(1000L);

        Mockito.verify(telegramAPI, Mockito.times(2)).sendMessage(1L, "allCommandsEverytime");
        Mockito.verify(telegramAPI, Mockito.times(0)).sendMessage(1L, "cmd2");
        Mockito.verify(telegramAPI, Mockito.times(2)).sendMessage(1L, "allCommandsFinal");
        Mockito.verify(telegramAPI, Mockito.times(1)).sendMessage(1L, "defaultHandler");
    }

    public class TestHandlers1 {
        @MessageHandler(
            filters = {FilterRegister.CommandFilter.class},
            params = @FilterParam(key = FilterParameter.COMMANDS, value = {"/cmd1", "cmd2"}),
            isFinal = false)
        public void allCommandsEverytime(Message message) {
            telegramAPI.sendMessage(1L, "allCommandsEverytime");
        }

        @MessageHandler(
            filters = {FilterRegister.CommandFilter.class},
            params = @FilterParam(key = FilterParameter.COMMANDS, value = {"cmd2"}),
            priority = 6,
            isFinal = false)
        public void cmd2After(Message message) {
            telegramAPI.sendMessage(1L, "cmd2");
        }
    }

    public class TestHandlers2 {
        @MessageHandler(
            filters = {FilterRegister.CommandFilter.class},
            priority = 5,
            params = @FilterParam(key = FilterParameter.COMMANDS, value = {"cmd1", "cmd2"}))
        public void allCommandsFinal(Message message) {
            telegramAPI.sendMessage(1L, "allCommandsFinal");
        }

        @MessageHandler(priority = 10)
        public void defaultHandler(Message message) {
            telegramAPI.sendMessage(1L, "defaultHandler");
        }
    }
}
