package backend.academy.scrapper.clients;

import backend.academy.dto.LinkUpdate;
import reactor.core.publisher.Mono;

public interface BotClient {
    Mono<Void> sendUpdate(LinkUpdate linkUpdate);
}
