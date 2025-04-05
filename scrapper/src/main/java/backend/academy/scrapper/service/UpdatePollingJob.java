package backend.academy.scrapper.service;

import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.service.parsers.LinkParsesContext;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@Log4j2
@RequiredArgsConstructor
public class UpdatePollingJob {
    private final LinkRepository linkRepository;
    private final LinkParsesContext linkParsesContext;
    private Instant lastUpdated = Instant.now();

    @Scheduled(cron = "#{@updateCron}")
    public void update() {
        log.info("Polling all links");
        Flux.fromIterable(linkRepository.findAll())
                .flatMap(link -> {
                    log.info("polling link {}", link.getUrl());
                    return linkParsesContext.updateLink(link, lastUpdated);
                })
                .then()
                .doFinally(signal -> {
                    log.info("Polling finished");
                    lastUpdated = Instant.now();
                })
                .subscribe();
    }
}
