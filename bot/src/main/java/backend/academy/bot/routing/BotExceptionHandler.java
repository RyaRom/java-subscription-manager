package backend.academy.bot.routing;

import backend.academy.bot.telegram.sdk.annotations.BotRouterAdvice;
import backend.academy.bot.telegram.sdk.annotations.ExceptionHandler;
import backend.academy.bot.telegram.sdk.utils.TelegramAPI;
import backend.academy.exception.BadLinkException;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@BotRouterAdvice
@Log4j2
public class BotExceptionHandler {
    private final TelegramAPI telegramAPI;

    @ExceptionHandler(Throwable.class)
    public Mono<Void> unknownError(Exception e, Update update) {
        log.error("In unknown error {}. update = {}", e, update);
        return telegramAPI.sendMessageAsync(update.message(), "Unexpected error: " + e.getMessage());
    }

    @ExceptionHandler(WebClientRequestException.class)
    public Mono<Void> cantConnect(WebClientRequestException e, Update update) {
        log.error("In cantConnect {}. update = {}", e, update);
        return telegramAPI.sendMessageAsync(update.message(), "Can't connect to internal server");
    }

    @ExceptionHandler(BadLinkException.class)
    public Mono<Void> badLink(BadLinkException e, Update update) {
        log.error("In badLink {}. update = {}", e, update);
        return telegramAPI.sendMessageAsync(
                update.message(), "Link is not supported incorrect or duplicated. Your links: /list");
    }
}
