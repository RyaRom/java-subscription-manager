package backend.academy.bot;

import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;

public class BotKeyboards {
    public static final String SKIP_TEXT = "Skip";

    public static ReplyKeyboardMarkup getSkipButton() {
        return new ReplyKeyboardMarkup(SKIP_TEXT)
            .oneTimeKeyboard(true)
            .resizeKeyboard(true)
            .selective(true);
    }
}
