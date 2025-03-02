package backend.academy.bot.rest;

import backend.academy.bot.telegram.utils.TelegramAPI;
import backend.academy.dto.LinkUpdate;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/bot/api")
@RequiredArgsConstructor
public class BotController {
    private final TelegramAPI telegramAPI;

    @PostMapping("/updates")
    public Mono<ResponseEntity<Void>> sendUpdates(@RequestBody LinkUpdate linkUpdate) {
        return telegramAPI
                .sendMessagesAsync(Flux.fromIterable(linkUpdate.tgChatIds()), getUpdateInfo(linkUpdate))
                .then(Mono.just(ResponseEntity.ok().build()));
    }

    public static @NotNull String getUpdateInfo(LinkUpdate linkUpdate) {
        return "Update in %s\n\n%s".formatted(linkUpdate.url(), linkUpdate.description());
    }
}
