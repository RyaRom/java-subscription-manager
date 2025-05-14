package backend.academy.scrapper.service.parsers;

import backend.academy.scrapper.clients.BotClient;
import backend.academy.scrapper.clients.GithubClient;
import backend.academy.scrapper.repository.dto.LinkType;
import backend.academy.scrapper.repository.dto.github.GithubFullInfo;
import backend.academy.scrapper.repository.entities.GithubInfoEntity;
import backend.academy.scrapper.repository.entities.LinkEntity;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    private static final SimpleDateFormat DATE_FORMAT =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
    private final GithubClient githubClient;
    private final BotClient botClient;

    @Override
    public boolean parse(LinkEntity link, List<String> tokens) {
        if (tokens.contains("github.com")) {
            link.setLinkType(LinkType.GITHUB);
            int siteIndex = tokens.indexOf("github.com");
            var info = new GithubInfoEntity(tokens.get(siteIndex + 1), tokens.get(siteIndex + 2));
            info.setLink(link);
            link.setLinkInfo(info);
            return true;
        }
        return false;
    }

    @Override
    public Mono<Boolean> update(LinkEntity link, Instant lastUpdated) {
        if (link.getLinkType() != LinkType.GITHUB) {
            return Mono.just(false);
        }
        if (link.getLinkInfo() instanceof GithubInfoEntity githubInfo) {
            return githubClient
                .getRepoActivities(
                    githubInfo.getOwner(), githubInfo.getRepo())
                .doOnNext(activity -> {
                    log.info("activity {}", activity);
                    log.info("time :{}", activity.timestamp().toInstant().atOffset(ZoneOffset.UTC));
                    log.info("last updated :{}", lastUpdated.atOffset(ZoneOffset.UTC));
                })
                .filter(activity -> activity.timestamp()
                    .toInstant()
                    .atOffset(ZoneOffset.UTC)
                    .isAfter(lastUpdated.atOffset(ZoneOffset.UTC)))
                .flatMap(activity ->
                    botClient.sendUpdate(GithubFullInfo.fromResponse(activity), link))
                .mergeWith(
                    githubClient.getRepoIssues(githubInfo.getOwner(), githubInfo.getRepo())
                        .filter(activity ->
                        {
                            try {
                                return DATE_FORMAT.parse(activity.updatedAt())
                                    .toInstant()
                                    .atOffset(ZoneOffset.UTC)
                                    .isAfter(lastUpdated.atOffset(ZoneOffset.UTC));
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .flatMap(activity ->
                            botClient.sendUpdate(GithubFullInfo.fromUpdate(activity, "issue"), link))
                ).mergeWith(
                    githubClient.getRepoPulls(githubInfo.getOwner(), githubInfo.getRepo())
                        .filter(activity ->
                        {
                            try {
                                return DATE_FORMAT.parse(activity.updatedAt())
                                    .toInstant()
                                    .atOffset(ZoneOffset.UTC)
                                    .isAfter(lastUpdated.atOffset(ZoneOffset.UTC));
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .flatMap(activity ->
                            botClient.sendUpdate(GithubFullInfo.fromUpdate(activity, "pr"), link))
                )
                .then(Mono.just(true));
        }

        throw new IllegalStateException("Parser doesn't work correctly");
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
