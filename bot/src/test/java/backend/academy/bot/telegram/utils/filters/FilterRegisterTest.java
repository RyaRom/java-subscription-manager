package backend.academy.bot.telegram.utils.filters;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pengrad.telegrambot.model.Message;
import java.util.Map;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

class FilterRegisterTest {

    @Test
    void testUrlFilter() {
        FilterRegister.UrlFilter filter = new FilterRegister.UrlFilter();
        Predicate<Message> predicate = filter.filter(Map.of());

        assertTrue(predicate.test(mockMessage("https://example.com")));
        assertTrue(predicate.test(mockMessage("http://example.com")));
        assertTrue(predicate.test(mockMessage("www.example.com")));
        assertTrue(predicate.test(mockMessage("https://sub.domain.com/path?query=1")));

        assertFalse(predicate.test(mockMessage("Just some text")));
        assertFalse(predicate.test(mockMessage("example.com")));
        assertFalse(predicate.test(mockMessage("http:/example.com")));
        assertFalse(predicate.test(mockMessage(null)));
        assertFalse(predicate.test(mockMessage("")));
    }

    private Message mockMessage(String text) {
        Message message = mock(Message.class);
        when(message.text()).thenReturn(text);
        return message;
    }
}
