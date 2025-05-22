package backend.academy.bot.telegram.sdk.utils;

import backend.academy.dto.LinkUpdate;
import backend.academy.exception.TelegramServerError;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import java.time.Duration;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

@Component
@Log4j2
public class TelegramAPI {
    private final TelegramBot telegramBot;

    public TelegramAPI(@Lazy TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    private static void logTelegramError(Throwable e) {
        log.error("Error sending message: {}", e.getMessage());
    }

    public Flux<Void> sendMessagesAsync(Flux<Long> chatIds, String text) {
        return chatIds.flatMap(chatId -> sendMessageAsyncWithRetry(chatId, text, 10));
    }

    public Flux<Void> sendMessagesAsync(LinkUpdate linkUpdate) {
        return sendMessagesAsync(Flux.fromIterable(linkUpdate.tgChatIds()), getUpdateInfo(linkUpdate));
    }

    public Mono<Void> sendMessageAsync(Long chatId, String text) {
        return Mono.fromCallable(() -> {
                    sendMessage(chatId, text);
                    return null;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    public Mono<Void> sendMessageAsyncWithRetry(Long chatId, String text, int retries) {
        return sendMessageAsync(chatId, text)
                .retryWhen(Retry.fixedDelay(retries, Duration.ofSeconds(1)))
                .onErrorMap(e -> {
                    log.error("Final failure after {} retries", retries, e);
                    return new TelegramServerError(e);
                });
    }

    public Mono<Void> sendMessageAsync(Message message, String text) {
        return sendMessageAsync(message.chat().id(), text);
    }

    public Mono<Void> sendMessageAsync(Message message, String text, Keyboard keyboard) {
        return Mono.fromRunnable(() -> sendMessage(message.chat().id(), text, keyboard))
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorMap(e -> {
                    logTelegramError(e);
                    return e;
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

    public static @NotNull String getUpdateInfo(LinkUpdate linkUpdate) {
        return "Update in %s\n\n%s".formatted(linkUpdate.url(), linkUpdate.description());
    }
}
