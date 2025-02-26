package backend.academy.bot.rest;

import backend.academy.bot.rest.dto.LinkUpdate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BotController {
    public ResponseEntity<Void> sendUpdates(@RequestBody LinkUpdate linkUpdate) {
        return ResponseEntity.ok().build();
    }
}
