package backend.academy.scrapper.service.parsers;

import backend.academy.exception.BadLinkException;
import backend.academy.scrapper.repository.entities.ChatIdEntity;
import backend.academy.scrapper.repository.entities.LinkEntity;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
@Log4j2
public class LinkParsesContext {
    private final List<AbstractParser> parserChain;

    public LinkEntity generateLink(String url, Long chatId) {
        var link = new LinkEntity();
        link.setUrl(url);
        link.setChatIds(List.of(new ChatIdEntity().setChatId(chatId)));
        List<String> tokens = List.of(url.split("/"));
        for (var parser : parserChain) {
            if (parser.parse(link, tokens)) {
                return link;
            }
        }
        throw new BadLinkException("Not a valid link");
    }

    public Mono<Void> updateLink(LinkEntity link, Instant lastUpdated) {
        log.info("polling link {}", link.getUrl());
        Mono<Boolean> wasUpdated = Flux.fromIterable(parserChain)
            .concatMap(it -> it.update(link, lastUpdated))
            .takeUntil(s -> s)
            .last(false);

        return wasUpdated.handle((result, sink) -> {
            if (!result) {
                sink.error(new BadLinkException("Link was saved without a type " + link.getLinkId()));
            }
        });
    }

    @PostConstruct
    private void init() {
        parserChain.sort(Comparator.comparingInt(AbstractParser::getOrder));
    }
}
