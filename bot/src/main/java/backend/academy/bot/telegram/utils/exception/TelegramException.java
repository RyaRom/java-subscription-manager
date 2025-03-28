package backend.academy.bot.telegram.utils.exception;

import com.pengrad.telegrambot.model.Update;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Getter
//not perfect but works
public class TelegramException extends RuntimeException {
    private final Update update;
    private final String reason;

    public TelegramException(Throwable cause, Update update, String reason) {
        super(cause);
        this.update = update;
        this.reason = reason;
    }
}
