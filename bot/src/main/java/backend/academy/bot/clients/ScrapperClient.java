package backend.academy.bot.clients;

import backend.academy.dto.ListLinkResponse;
import reactor.core.publisher.Mono;

public interface ScrapperClient {
    Mono<ListLinkResponse> getLinks(Long chatId);
}
