package backend.academy.bot.telegram.utils;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@Log4j2
@RequiredArgsConstructor
public class TelegramAPI {

    @Lazy
    @Autowired
    // field injection to avoid circular dependency
    private TelegramBot telegramBot;

    private static void logTelegramError(Throwable e) {
        log.error("Error sending message: {}", e.getMessage());
    }

    public Flux<Void> sendMessagesAsync(Flux<Long> chatIds, String text) {
        return chatIds.flatMap(chatId -> sendMessageAsync(chatId, text));
    }

    public Mono<Void> sendMessageAsync(Long chatId, String text) {
        return Mono.fromRunnable(() -> sendMessage(chatId, text))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    logTelegramError(e);
                    return Mono.empty();
                })
                .then();
    }

    public Mono<Void> sendMessageAsync(Message message, String text) {
        return sendMessageAsync(message.chat().id(), text);
    }

    public Mono<Void> sendMessageAsync(Message message, String text, Keyboard keyboard) {
        return Mono.fromRunnable(() -> sendMessage(message.chat().id(), text, keyboard))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    logTelegramError(e);
                    return Mono.empty();
                })
                .then();
    }

    public void sendMessage(Long chatId, String text) {
        SendMessage request = new SendMessage(chatId, text).parseMode(ParseMode.HTML);
        telegramBot.execute(request);
    }

    public void sendMessage(Long chatId, String text, Keyboard keyboard) {
        SendMessage request =
                new SendMessage(chatId, text).parseMode(ParseMode.HTML).replyMarkup(keyboard);
        telegramBot.execute(request);
    }
}
