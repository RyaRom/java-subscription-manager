package backend.academy.scrapper.service;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinkResponse;
import backend.academy.exception.LinkDuplicatedException;
import backend.academy.exception.ResourceNotFoundException;
import backend.academy.scrapper.repository.links.LinkRepository;
import backend.academy.scrapper.repository.links.entities.LinkEntity;
import backend.academy.scrapper.service.parsers.LinkParsesContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
@Log4j2
public class LinksService {
    private final LinkRepository linkRepository;
    private final LinkParsesContext linkContext;

    public Mono<ListLinkResponse> getLinks(Long chatId) {
        return Mono.fromCallable(() -> {
            var links = linkRepository.findWithChatId(chatId).stream()
                    .map(LinkEntity::toLinkResponse)
                    .toList();
            return new ListLinkResponse(links, links.size());
        });
    }

    public Mono<LinkResponse> addLink(Long chatId, AddLinkRequest request) {
        var savedLink = linkRepository
                .findByUrl(request.getLink())
                .map(link -> {
                    if (link.getChatIds().stream().anyMatch(it -> it.getChatId().equals(chatId))) {
                        throw new LinkDuplicatedException("Link already exists");
                    }
                    linkRepository.addChatId(link.getLinkId(), chatId);
                    return link;
                })
                .orElseGet(() -> {
                    LinkEntity link = linkContext.generateLink(request.getLink(), chatId);
                    linkRepository.save(link);
                    return link;
                });

        return Mono.just(savedLink.toLinkResponse());
    }

    public Mono<LinkResponse> removeLink(String link) {
        log.info("Remove link {}", link);
        var deletedLink =
                linkRepository.deleteByUrl(link).orElseThrow(() -> new ResourceNotFoundException("not found" + link));
        return Mono.just(deletedLink.toLinkResponse());
    }
}
