package backend.academy.bot.clients;

import backend.academy.dto.AddLinkRequest;
import reactor.core.publisher.Mono;

public interface ScrapperPublisher {
    Mono<Void> registerChat(Long chatId);

    Mono<Void> addLink(Long chatId, AddLinkRequest addLinkRequest);

    Mono<Void> removeLink(Long chatId, String link);
}
