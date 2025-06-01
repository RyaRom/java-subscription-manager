package backend.academy.bot.telegram.sdk.utils;

import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Signal;

@UtilityClass
public class ReactorUtils {
    public static final String CHAT_ID_CONTEXT = "chatId";

    public static <T> @NotNull Optional<T> getReactorContext(Signal<?> signal, String key) {
        return signal.getContextView().getOrEmpty(key);
    }

    public static <T> @NotNull Optional<T> getChatIdContext(Signal<?> signal) {
        return getReactorContext(signal, CHAT_ID_CONTEXT);
    }
}
