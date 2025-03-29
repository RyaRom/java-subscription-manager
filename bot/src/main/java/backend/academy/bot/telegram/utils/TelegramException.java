package backend.academy.bot.telegram.utils;

import com.pengrad.telegrambot.model.Update;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Wrapper for all exceptions after receiving update for storing telegram context
 */
@Getter
@RequiredArgsConstructor
public class TelegramException extends RuntimeException {
    private final Update update;
    private final Throwable reason;
}
