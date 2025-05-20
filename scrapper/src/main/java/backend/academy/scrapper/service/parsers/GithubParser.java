package backend.academy.scrapper.service.parsers;

import backend.academy.dto.LinkUpdate;
import backend.academy.scrapper.clients.BotClient;
import backend.academy.scrapper.clients.GithubHttpClient;
import backend.academy.scrapper.repository.links.dto.LinkType;
import backend.academy.scrapper.repository.links.dto.github.GithubFullInfo;
import backend.academy.scrapper.repository.links.entities.GithubInfoEntity;
import backend.academy.scrapper.repository.links.entities.LinkEntity;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Log4j2
@RequiredArgsConstructor
public class GithubParser implements AbstractParser {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
    private final GithubHttpClient githubHttpClient;
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
            var activities = githubHttpClient
                .getRepoActivities(githubInfo.getOwner(), githubInfo.getRepo())
                .onErrorResume(it ->{
                    log.error("Error while getting github activities", it);
                    return Mono.empty();
                })
                .doOnNext(activity -> {
                    log.info("activity {}", activity);
                    log.info("time :{}", activity.timestamp().toInstant().atOffset(ZoneOffset.UTC));
                    log.info("last updated :{}", lastUpdated.atOffset(ZoneOffset.UTC));
                })
                .filter(activity -> activity.timestamp()
                    .toInstant()
                    .atOffset(ZoneOffset.UTC)
                    .isAfter(lastUpdated.atOffset(ZoneOffset.UTC)))
                .flatMap(activity -> sendGhUpdate(GithubFullInfo.fromResponse(activity), link));
            var issues = githubHttpClient
                .getRepoIssues(githubInfo.getOwner(), githubInfo.getRepo())
                .onErrorResume(it -> {
                    log.error("Error while getting github issues", it);
                    return Mono.empty();
                })
                .filter(activity -> {
                    try {
                        return DATE_FORMAT
                            .parse(activity.updatedAt())
                            .toInstant()
                            .atOffset(ZoneOffset.UTC)
                            .isAfter(lastUpdated.atOffset(ZoneOffset.UTC));
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                })
                .flatMap(activity -> sendGhUpdate(GithubFullInfo.fromUpdate(activity, "issue"), link));
            var pulls = githubHttpClient
                .getRepoPulls(githubInfo.getOwner(), githubInfo.getRepo())
                .onErrorResume(it -> {
                    log.error("Error while getting github pulls", it);
                    return Mono.empty();
                })
                .filter(activity -> {
                    try {
                        return DATE_FORMAT
                            .parse(activity.updatedAt())
                            .toInstant()
                            .atOffset(ZoneOffset.UTC)
                            .isAfter(lastUpdated.atOffset(ZoneOffset.UTC));
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                })
                .flatMap(activity -> sendGhUpdate(GithubFullInfo.fromUpdate(activity, "pr"), link));

            return Flux.merge(activities, issues, pulls).then(Mono.just(true));
        }

        throw new IllegalStateException("Parser doesn't work correctly");
    }

    public Mono<Void> sendGhUpdate(GithubFullInfo githubActivity, LinkEntity link) {
        if (githubActivity.type().isEmpty()) {
            log.warn("Unknown type in github update {}", githubActivity);
        }
        LinkUpdate linkUpdate = LinkUpdate.builder()
            .linkId(link.getLinkId())
            .url(link.getUrl())
            .tgChatIds(link.getChatIdList())
            .description(getGithubUpdate(githubActivity))
            .build();
        return botClient.sendUpdate(linkUpdate);
    }

    private String getGithubUpdate(GithubFullInfo info) {
        return String.format(
            """
                New Github update (%s): %s
                User: %s
                Text: %s""",
            info.type(), info.title(), info.username(), info.body());
    }


    @Override
    public int getOrder() {
        return 0;
    }
}
