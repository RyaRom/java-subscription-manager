package backend.academy.scrapper.service;

import backend.academy.scrapper.config.DataConnectionProperties;
import backend.academy.scrapper.repository.links.LinkRepository;
import backend.academy.scrapper.repository.links.entities.LinkEntity;
import backend.academy.scrapper.service.parsers.LinkParsesContext;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@Log4j2
@RequiredArgsConstructor
public class UpdatePollingJob {
    private final DataConnectionProperties dataConnectionProperties;
    private final LinkRepository linkRepository;
    private final LinkParsesContext linkParsesContext;
    private Instant lastUpdated = Instant.now();

    @Scheduled(cron = "#{@updateCron}")
    public void update() {
        lastUpdated = Instant.now().minus(10000, ChronoUnit.DAYS);
        log.info("Polling all links");
        Flux.generate(() -> -1L, (lastId, sink) -> {
                    List<LinkEntity> page =
                            linkRepository.findAllPaginated(lastId, dataConnectionProperties.linksPagesize());
                    log.info("Processing page with lastId: {}", lastId);
                    if (page.isEmpty()) {
                        log.info("Finished processing pages");
                        sink.complete();
                        return lastId;
                    }
                    page.forEach(link -> {
                        sink.next(link);
                        linkParsesContext.updateLink(link, lastUpdated).subscribe();
                    });
                    return page.getLast().getLinkId();
                })
                .doFinally(signal -> {
                    log.info("Polling finished {}", signal);
                    lastUpdated = Instant.now();
                })
                .subscribe();
    }
}
