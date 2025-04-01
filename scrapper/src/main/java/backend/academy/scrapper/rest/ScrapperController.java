package backend.academy.scrapper.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/scrapper/api")
public class ScrapperController {

    @PostMapping("/tg-chat/{chatId}")
    public Mono<ResponseEntity<Void>> registerChat(@PathVariable Long chatId) {
        log.info("Chat registered {}", chatId);
        return Mono.just(ResponseEntity.ok().build());
    }

    @DeleteMapping("/tg-chat/{chatId}")
    public Mono<ResponseEntity<Void>> deleteChat(@PathVariable Long chatId) {
        log.info("Chat deleted {}", chatId);
        return Mono.just(ResponseEntity.ok().build());
    }
}
