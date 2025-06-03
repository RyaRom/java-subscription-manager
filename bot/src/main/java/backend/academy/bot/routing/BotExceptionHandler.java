package backend.academy.bot.routing;

import backend.academy.bot.telegram.sdk.annotations.BotRouterAdvice;
import backend.academy.bot.telegram.sdk.annotations.ExceptionHandler;
import backend.academy.bot.telegram.sdk.utils.TelegramAPI;
import backend.academy.exception.BadLinkException;
import backend.academy.exception.LinkDuplicatedException;
import backend.academy.exception.ServerUnavailableException;
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

    @ExceptionHandler({Throwable.class, Exception.class})
    public Mono<Void> unknownError(Exception e, Update update) {
        log.error("In unknown error {}. update = {}", e, update);
        return telegramAPI.sendMessageAsync(
                update.message(), "Unexpected error: " + e.getMessage() + "\n\n" + e.getClass());
    }

    @ExceptionHandler({
        WebClientRequestException.class,
        ServerUnavailableException.class,
    })
    public Mono<Void> cantConnect(Exception e, Update update) {
        log.error("In cantConnect {}. update = {}", e, update);
        return telegramAPI.sendMessageAsync(update.message(), "Can't connect to internal server");
    }

    @ExceptionHandler(BadLinkException.class)
    public Mono<Void> badLink(BadLinkException e, Update update) {
        log.error("In badLink {}. update = {}", e, update);
        return telegramAPI.sendMessageAsync(update.message(), "Link is incorrect. Your links: /list");
    }

    @ExceptionHandler(LinkDuplicatedException.class)
    public Mono<Void> duplicatedLink(LinkDuplicatedException e, Update update) {
        log.error("In duplicatedLink {}. update = {}", e, update);
        return telegramAPI.sendMessageAsync(update.message(), "Link already exist. Your links: /list");
    }
}
