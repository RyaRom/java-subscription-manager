package backend.academy.scrapper.service;

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
import static backend.academy.scrapper.repository.dto.Link.GithubInfo.getGithubInfo;

@Component
@Log4j2
@RequiredArgsConstructor
public class UpdatePollingJob {
    private final BotClient botClient;
    private final LinkRepository linkRepository;
    private final GithubClient githubClient;
    private final StackOverflowClient stackOverflowClient;
    private Instant lastUpdated = Instant.now();

    @Scheduled(fixedRate = 1000 * 60 * 60 * 12)
    public void update() {
        lastUpdated = Instant.now();
        log.info("Polling all links");
        Flux.fromIterable(linkRepository.findAll())
            .flatMap(this::updateLink)
            .then()
            .subscribe();
    }

    //TODO refactor
    public Mono<Void> updateLink(Link link) {
        Link.Type linkType = link.linkType();
        switch (linkType) {
            case GITHUB -> {
                if (link.githubInfo() == null) {
                    link.githubInfo(getGithubInfo(link.url()));
                }
                return githubClient
                    .getRepoActivities(
                        link.githubInfo().owner(), link.githubInfo().repo())
                    .flatMapMany(res -> Flux.fromIterable(res.activities()))
                    .filter(activity -> activity.timestamp().isAfter(lastUpdated.atOffset(ZoneOffset.UTC)))
                    .flatMap(activity -> botClient.sendUpdate(activity, link))
                    .then();
            }
            case STACK_OVERFLOW -> {
                if (link.stackOverflowInfo() == null) {
                    link.stackOverflowInfo(Link.StackOverflowInfo.getStackOverflowInfo(link.url()));
                }
                return stackOverflowClient
                    .getStackOverflowNewAnswers(link.stackOverflowInfo().questionId(), lastUpdated)
                    .flatMapMany(res -> Flux.fromIterable(res.items()))
                    .flatMap(answer -> botClient.sendUpdate(answer, link))
                    .then();
            }
            default -> {
                log.error("Unknown link type {}", link.linkId());
                throw new IllegalArgumentException("Unknown link type " + link.linkId());
            }
        }
    }
}
