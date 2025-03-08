package backend.academy.scrapper.service;

import static backend.academy.scrapper.repository.dto.Link.GithubInfo.parseGithubInfo;
import static backend.academy.scrapper.repository.dto.Link.StackOverflowInfo.parseStackOverflowInfo;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinkResponse;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.dto.Link;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
@Log4j2
public class ScrapperService {
    private final LinkRepository linkRepository;

    public Mono<ListLinkResponse> getLinks(Long chatId) {
        return Mono.fromCallable(() -> {
            var links = linkRepository.findAll().stream()
                    .filter(link -> link.getChatIds().contains(chatId))
                    .map(Link::toLinkResponse)
                    .toList();
            return new ListLinkResponse(links, links.size());
        });
    }

    public Mono<LinkResponse> addLink(Long chatId, AddLinkRequest request) {
        var savedLink = linkRepository
                .find(request.getLink())
                .map(link -> {
                    link.getChatIds().add(chatId);
                    linkRepository.save(link);
                    return link;
                })
                .orElseGet(() -> {
                    Link link = generateLink(request, chatId);
                    linkRepository.save(link);
                    return link;
                });

        return Mono.just(savedLink.toLinkResponse());
    }

    private Link generateLink(AddLinkRequest request, Long chatId) {
        var builder = Link.builder();
        String url = request.getLink();
        builder.url(url);
        builder.chatIds(new HashSet<>(List.of(chatId)));
        List<String> parsed = List.of(url.split("/"));
        if (parsed.contains("github.com")) {
            builder.linkType(Link.Type.GITHUB);
            builder.githubInfo(parseGithubInfo(url));
        } else if (parsed.contains("stackoverflow.com")) {
            builder.linkType(Link.Type.STACK_OVERFLOW);
            builder.stackOverflowInfo(parseStackOverflowInfo(url));
        } else {
            throw new IllegalArgumentException("Not a valid link");
        }

        return builder.build();
    }

    public Mono<LinkResponse> removeLink(String link) {
        log.info("Remove link {}", link);
        var deletedLink = linkRepository.delete(link).orElseThrow(NotFoundException::new);
        return Mono.just(deletedLink.toLinkResponse());
    }
}
