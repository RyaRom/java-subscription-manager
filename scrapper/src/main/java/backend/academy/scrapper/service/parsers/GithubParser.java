package backend.academy.scrapper.service.parsers;

import backend.academy.scrapper.clients.BotClient;
import backend.academy.scrapper.clients.GithubClient;
import backend.academy.scrapper.repository.dto.Link;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Log4j2
@RequiredArgsConstructor
public class GithubParser implements AbstractParser {
    private final GithubClient githubClient;
    private final BotClient botClient;

    @Override
    public boolean parse(Link.LinkBuilder link, List<String> tokens) {
        if (tokens.contains("github.com")) {
            link.linkType(Link.Type.GITHUB);
            int siteIndex = tokens.indexOf("github.com");
            var info = new Link.GithubInfo(tokens.get(siteIndex + 1), tokens.get(siteIndex + 2));
            link.githubInfo(info);
            return true;
        }
        return false;
    }

    @Override
    public Mono<Boolean> update(Link link, Instant lastUpdated) {
        if (link.getLinkType() != Link.Type.GITHUB) {
            return Mono.just(false);
        }
        if (link.getGithubInfo() == null) {
            throw new IllegalStateException("Parser doesn't work correctly");
        }
        return githubClient
            .getRepoActivities(
                link.getGithubInfo().owner(), link.getGithubInfo().repo())
            .doOnNext(activity -> {
                log.info("activity {}", activity);
                log.info("time :{}", activity.timestamp().toInstant().atOffset(ZoneOffset.UTC));
                log.info("last updated :{}", lastUpdated.atOffset(ZoneOffset.UTC));
            })
            .filter(activity -> activity.timestamp()
                .toInstant()
                .atOffset(ZoneOffset.UTC)
                .isAfter(lastUpdated.atOffset(ZoneOffset.UTC)))
            .flatMap(activity -> botClient.sendUpdate(activity, link))
            .then(Mono.just(true));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
