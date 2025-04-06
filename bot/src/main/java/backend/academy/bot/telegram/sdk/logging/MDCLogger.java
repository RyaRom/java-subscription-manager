package backend.academy.bot.telegram.sdk.logging;

import java.util.Optional;
import java.util.function.Consumer;
import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;
import reactor.core.publisher.Signal;

@Log4j2
public class MDCLogger {
    public static <T> Consumer<Signal<T>> logOnNext(Consumer<T> logStatement) {
        return signal -> {
            if (!signal.isOnNext()) {
                return;
            }
            Optional<Long> chatId = signal.getContextView().getOrEmpty("chatId");

            chatId.ifPresentOrElse(tpim -> {
                    try (MDC.MDCCloseable cMdc = MDC.putCloseable("chatId", String.valueOf(tpim))) {
                        logStatement.accept(signal.get());
                    }
                },
                () -> logStatement.accept(signal.get()));
        };
    }
}
