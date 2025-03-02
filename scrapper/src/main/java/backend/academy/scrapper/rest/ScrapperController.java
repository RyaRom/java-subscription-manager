package backend.academy.scrapper.rest;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinkResponse;
import backend.academy.dto.RemoveLinkRequest;
import backend.academy.scrapper.service.ScrapperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/scrapper/api")
public class ScrapperController {
    private final ScrapperService scrapperService;

    @PostMapping("/tg-chat/{chatId}")
    public ResponseEntity<Void> registerChat(@PathVariable Long chatId) {
        log.info("Chat registered {}", chatId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tg-chat/{chatId}")
    public ResponseEntity<Void> deleteChat(@PathVariable Long chatId) {
        log.info("Chat deleted {}", chatId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/links")
    public Mono<ResponseEntity<ListLinkResponse>> getLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        log.info("Get links for chat {}", chatId);
        return scrapperService.getLinks(chatId)
            .map(ResponseEntity::ok);
    }
    //TODO finish operations for bot

    @PostMapping("/links")
    public ResponseEntity<LinkResponse> addLink(@RequestHeader("Tg-Chat-Id") Long chatId,
                                                @RequestBody AddLinkRequest request) {
        log.info("Add link for chat {}", chatId);
        log.info("request {}", request);
        return ResponseEntity.ok(new LinkResponse(null, null, null, null));
    }

    @DeleteMapping("/links")
    public ResponseEntity<LinkResponse> removeLink(@RequestHeader("Tg-Chat-Id") Long chatId,
                                                   @RequestBody RemoveLinkRequest request) {
        log.info("Remove link for chat {}", chatId);
        return ResponseEntity.ok(new LinkResponse(null, null, null, null));
    }
}
