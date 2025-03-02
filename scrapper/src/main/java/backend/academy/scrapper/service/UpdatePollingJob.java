package backend.academy.scrapper.service;

import backend.academy.scrapper.clients.BotClient;
import backend.academy.scrapper.clients.GithubClient;
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
import static backend.academy.scrapper.repository.dto.GithubInfo.getGithubInfo;

@Component
@Log4j2
@RequiredArgsConstructor
public class UpdatePollingJob {
    private final BotClient botClient;
    private final LinkRepository linkRepository;
    private final GithubClient githubClient;
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

    public Mono<Void> updateLink(Link link) {
        Link.Type linkType = link.linkType();
        if (linkType == Link.Type.GITHUB) {
            if (link.githubInfo() == null) {
                link.githubInfo(getGithubInfo(link.url()));
            }
            return githubClient.getRepoActivities(link.githubInfo().owner(), link.githubInfo().repo())
                .flatMapMany(res -> Flux.fromIterable(res.activities()))
                .filter(activity -> activity.timestamp().isAfter(lastUpdated.atOffset(ZoneOffset.UTC)))
                .flatMap(activity -> botClient.sendUpdate(activity, link))
                .then();
        } else if (linkType == Link.Type.STACK_OVERFLOW) {
            // TODO: STACK OVERFLOW Client
        }
        return Mono.empty();
    }

}
