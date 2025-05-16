package backend.academy.scrapper.service.parsers;

import backend.academy.scrapper.repository.links.entities.LinkEntity;
import java.time.Instant;
import java.util.List;
import reactor.core.publisher.Mono;

public interface AbstractParser {
    boolean parse(LinkEntity link, List<String> tokens);

    Mono<Boolean> update(LinkEntity link, Instant lastUpdated);

    /**
     * if order in chain matters
     */
    int getOrder();
}
