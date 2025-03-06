package backend.academy.scrapper.service;

import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinkResponse;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.dto.Link;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.ArrayList;
import java.util.List;
import static backend.academy.scrapper.repository.dto.GithubInfo.getGithubInfo;

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

    public Mono<LinkResponse> addLink(Long chatId, AddLinkRequest request) {
        var savedLink = linkRepository.find(chatId);
        if (savedLink.isPresent()) {
            Link link = savedLink.get();
            link.chatIds().add(chatId);
            linkRepository.save(link);
            return Mono.just(link.toLinkResponse());
        }

        Link link = generateLink(request, chatId);
        linkRepository.save(link);
        return Mono.just(link.toLinkResponse());
    }

    private Link generateLink(AddLinkRequest request, Long chatId) {
        var builder = Link.builder();
        String url = request.link();
        builder.url(url);
        builder.chatIds(new ArrayList<>(List.of(chatId)));
        List<String> parsed = List.of(url.split("/"));
        if (parsed.contains("github.com")){
            builder.linkType(Link.Type.GITHUB);
            builder.githubInfo(getGithubInfo(url));
        }else if (parsed.contains("stackoverflow.com")) {
            builder.linkType(Link.Type.STACK_OVERFLOW);
        }

        return builder.build();
    }

    public Mono<LinkResponse> removeLink(String link) {
        var deletedLink = linkRepository.delete(link);
        if (deletedLink.isEmpty()){
            throw new NotFoundException();
        }
        return Mono.just(deletedLink.get().toLinkResponse());
    }
}
