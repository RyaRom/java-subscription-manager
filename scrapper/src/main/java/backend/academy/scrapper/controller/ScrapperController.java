package backend.academy.scrapper.controller;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.dto.ListLinkResponse;
import backend.academy.scrapper.dto.RemoveLinkRequest;
import java.util.ArrayList;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScrapperController {
    @PostMapping("/tg-chat/{chatId}")
    public ResponseEntity<String> registerChat(@PathVariable Long chatId) {
        return ResponseEntity.ok("Чат зарегистрирован");
    }

    @DeleteMapping("/tg-chat/{chatId}")
    public ResponseEntity<String> deleteChat(@PathVariable Long chatId) {
        return ResponseEntity.ok("Чат успешно удалён");
    }

    @GetMapping("/links")
    public ResponseEntity<ListLinkResponse> getLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        return ResponseEntity.ok(new ListLinkResponse(new ArrayList<>(), 0));
    }

    @PostMapping("/links")
    public ResponseEntity<LinkResponse> addLink(@RequestHeader("Tg-Chat-Id") Long chatId,
                                                @RequestBody AddLinkRequest request) {
        return ResponseEntity.ok(new LinkResponse(null, null, null, null));
    }

    @DeleteMapping("/links")
    public ResponseEntity<LinkResponse> removeLink(@RequestHeader("Tg-Chat-Id") Long chatId,
                                                   @RequestBody RemoveLinkRequest request) {
        return ResponseEntity.ok(new LinkResponse(null, null, null, null));
    }
}
