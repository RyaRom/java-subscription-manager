package backend.academy.bot.clients;

import backend.academy.dto.ListLinkResponse;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ScrapperHttpCached implements ScrapperClient {
    private final ScrapperHttpClient scrapperHttpClient;

    @Override
    public Mono<ListLinkResponse> getLinks(Long chatId) {
        return null;
    }
}
