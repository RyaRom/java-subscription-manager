package backend.academy.bot;

import com.pengrad.telegrambot.model.Message;

@FunctionalInterface
public interface MessageHandler {
    void handle(Message message);
}
