package backend.academy.scrapper.service;

import static backend.academy.scrapper.repository.dto.Link.StackOverflowInfo.parseStackOverflowInfo;

import backend.academy.scrapper.clients.BotClient;
import backend.academy.scrapper.clients.GithubClient;
import backend.academy.scrapper.clients.StackOverflowClient;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.dto.Link;
import java.time.Instant;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Log4j2
@RequiredArgsConstructor
public class UpdatePollingJob {
    private final BotClient botClient;
    private final LinkRepository linkRepository;
    private final GithubClient githubClient;
    private final StackOverflowClient stackOverflowClient;
    private Instant lastUpdated = Instant.now();

    @Scheduled(cron = "#{@updateCron}")
    public void update() {
        log.info("Polling all links");
        Flux.fromIterable(linkRepository.findAll())
                .flatMap(this::updateLink)
                .then()
                .doFinally(signal -> {
                    log.info("Polling finished");
                    lastUpdated = Instant.now();
                })
                .subscribe();
    }

    // TODO refactor
    public Mono<Void> updateLink(Link link) {
        log.info("polling link {}", link.getUrl());
        Link.Type getLinkType = link.getLinkType();
        switch (getLinkType) {
            case GITHUB -> {
                if (link.getGithubInfo() == null) {
                    link.setGithubInfo(Link.GithubInfo.parseGithubInfo(link.getUrl()));
                }
                return githubClient
                        .getRepoActivities(
                                link.getGithubInfo().owner(),
                                link.getGithubInfo().repo())
                        .doOnNext(activity -> {
                            log.info("activity {}", activity);
                            log.info(
                                    "time :{}", activity.timestamp().toInstant().atOffset(ZoneOffset.UTC));
                            log.info("last updated :{}", lastUpdated.atOffset(ZoneOffset.UTC));
                        })
                        .filter(activity -> activity.timestamp()
                                .toInstant()
                                .atOffset(ZoneOffset.UTC)
                                .isAfter(lastUpdated.atOffset(ZoneOffset.UTC)))
                        .flatMap(activity -> botClient.sendUpdate(activity, link))
                        .then();
            }
            case STACK_OVERFLOW -> {
                if (link.getStackOverflowInfo() == null) {
                    link.setStackOverflowInfo(parseStackOverflowInfo(link.getUrl()));
                }
                return stackOverflowClient
                        .getStackOverflowNewAnswers(link.getStackOverflowInfo().questionId(), lastUpdated)
                        .flatMapMany(res -> Flux.fromIterable(res.items()))
                        .doOnNext(activity -> log.info("so update {}", activity))
                        .flatMap(answer -> botClient.sendUpdate(answer, link))
                        .then();
            }
            default -> {
                log.error("Unknown link type {}", link.getLinkId());
                throw new IllegalArgumentException("Unknown link type " + link.getLinkId());
            }
        }
    }
}
