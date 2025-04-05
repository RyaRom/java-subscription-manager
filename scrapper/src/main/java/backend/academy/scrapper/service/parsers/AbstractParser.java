package backend.academy.scrapper.service.parsers;

import backend.academy.scrapper.repository.dto.Link;
import java.time.Instant;
import java.util.List;
import reactor.core.publisher.Mono;

public interface AbstractParser {
    boolean parse(Link.LinkBuilder link, List<String> tokens);

    Mono<Boolean> update(Link link, Instant lastUpdated);

    /** if order in chain matters */
    int getOrder();
}
