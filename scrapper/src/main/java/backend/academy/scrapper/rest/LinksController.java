package backend.academy.scrapper.rest;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinkResponse;
import backend.academy.dto.RemoveLinkRequest;
import backend.academy.scrapper.service.LinksService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/scrapper/api/links")
public class LinksController {
    private final LinksService linksService;

    @GetMapping
    public Mono<ResponseEntity<ListLinkResponse>> getLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        log.info("Get links for chat {}", chatId);
        return linksService.getLinks(chatId).map(ResponseEntity::ok);
    }

    @PostMapping
    public Mono<ResponseEntity<LinkResponse>> addLink(
        @RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody AddLinkRequest request) {
        log.info("Add link for chat {}", chatId);
        log.info("request {}", request);
        return linksService.addLink(chatId, request).map(ResponseEntity::ok);
    }

    @DeleteMapping
    public Mono<ResponseEntity<LinkResponse>> removeLink(
        @RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody RemoveLinkRequest request) {
        log.info("Remove link for chat {}", chatId);
        return linksService.removeLink(request.link()).map(ResponseEntity::ok);
    }
}

