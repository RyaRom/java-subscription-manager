package backend.academy.bot.telegram.sdk.logging;

import java.util.Optional;
import java.util.function.Consumer;
import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;
import reactor.core.publisher.Signal;
import static backend.academy.bot.telegram.sdk.utils.ReactorUtils.CHAT_ID_CONTEXT;
import static backend.academy.bot.telegram.sdk.utils.ReactorUtils.getReactorContext;

@Log4j2
public class MDCLogger {


    public static <T> Consumer<Signal<T>> logOnNext(Consumer<T> logStatement) {
        return signal -> {
            if (!signal.isOnNext()) {
                return;
            }
            Optional<Long> chatId = getReactorContext(signal, CHAT_ID_CONTEXT);

            chatId.ifPresentOrElse(
                tpim -> {
                    try (MDC.MDCCloseable cMdc = MDC.putCloseable(CHAT_ID_CONTEXT, String.valueOf(tpim))) {
                        logStatement.accept(signal.get());
                    }
                },
                () -> logStatement.accept(signal.get()));
        };
    }

    public static <T> Consumer<Signal<T>> logOnError(Consumer<T> logStatement) {
        return signal -> {
            if (!signal.isOnError()) {
                return;
            }
            Optional<Long> chatId = getReactorContext(signal, CHAT_ID_CONTEXT);

            chatId.ifPresentOrElse(
                tpim -> {
                    try (MDC.MDCCloseable cMdc = MDC.putCloseable(CHAT_ID_CONTEXT, String.valueOf(tpim))) {
                        logStatement.accept(signal.get());
                    }
                },
                () -> logStatement.accept(signal.get()));
        };
    }
}
