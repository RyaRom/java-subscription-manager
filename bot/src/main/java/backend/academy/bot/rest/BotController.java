package backend.academy.bot.rest;

import backend.academy.bot.telegram.sdk.utils.TelegramAPI;
import backend.academy.dto.LinkUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Log4j2
@RestController
@RequestMapping("/bot/api")
@RequiredArgsConstructor
@SuppressWarnings("VA_FORMAT_STRING_USES_NEWLINE")
public class BotController {
    private final TelegramAPI telegramAPI;

    @PostMapping("/updates")
    public Mono<ResponseEntity<Void>> sendUpdates(@RequestBody LinkUpdate linkUpdate) {
        log.info("Got update in link {}, {}", linkUpdate.linkId(), linkUpdate.url());

        return telegramAPI.sendMessagesAsync(linkUpdate)
            .then(Mono.just(ResponseEntity.ok().build()));
    }
}
