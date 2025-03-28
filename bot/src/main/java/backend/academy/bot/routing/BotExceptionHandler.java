package backend.academy.bot.routing;

import backend.academy.bot.telegram.utils.TelegramAPI;
import backend.academy.bot.telegram.utils.annotations.BotRouterAdvice;
import backend.academy.bot.telegram.utils.annotations.ExceptionHandler;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@BotRouterAdvice
@Log4j2
public class BotExceptionHandler {
    private final TelegramAPI telegramAPI;

    @ExceptionHandler(Exception.class)
    public Mono<Void> unknownError(Exception e, Update update){
        log.error("In unknown error {}. update = {}", e, update);
        return telegramAPI.sendMessageAsync(update.message(), "Unexpected error: " + e.getMessage());
    }
}
