package backend.academy.scrapper.service;

import backend.academy.dto.ListLinkResponse;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.dto.Link;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class ScrapperService {
    private final LinkRepository linkRepository;

    public Mono<ListLinkResponse> getLinks(Long chatId) {
        return Mono.fromCallable(() -> {
            var links = linkRepository.findAll().stream()
                    .filter(link -> link.chatIds().contains(chatId))
                    .map(Link::toLinkResponse)
                    .toList();
            return new ListLinkResponse(links, links.size());
        });
    }
}
