package backend.academy.scrapper.service.parsers;

import backend.academy.scrapper.repository.dto.LinkDto;
import java.time.Instant;
import java.util.List;
import reactor.core.publisher.Mono;

public interface AbstractParser {
    boolean parse(LinkDto.LinkDtoBuilder link, List<String> tokens);

    Mono<Boolean> update(LinkDto link, Instant lastUpdated);

    /**
     * if order in chain matters
     */
    int getOrder();
}
