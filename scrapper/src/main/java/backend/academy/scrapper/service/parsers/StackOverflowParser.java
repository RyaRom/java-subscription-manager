package backend.academy.scrapper.service.parsers;

import backend.academy.scrapper.clients.BotClient;
import backend.academy.scrapper.clients.StackOverflowClient;
import backend.academy.scrapper.repository.entities.LinkEntity;
import backend.academy.scrapper.repository.dto.LinkType;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Log4j2
@RequiredArgsConstructor
public class StackOverflowParser implements AbstractParser {
    private final StackOverflowClient stackOverflowClient;
    private final BotClient botClient;

    @Override
    public boolean parse(LinkEntity link, List<String> tokens) {
        if (tokens.contains("stackoverflow.com")) {
            link.setLinkType(LinkType.STACK_OVERFLOW);
            int siteIndex = tokens.indexOf("stackoverflow.com");
            var info = new backend.academy.scrapper.repository.entities.LinkEntity.StackOverflowInfo(Long.parseLong(tokens.get(siteIndex + 2)));
            link.setLinkInfo(info);
            return true;
        }
        return false;
    }

    @Override
    public Mono<Boolean> update(LinkEntity link, Instant lastUpdated) {
        if (link.getLinkType() != LinkType.STACK_OVERFLOW) {
            return Mono.just(false);
        }
        if (link.getLinkInfo() instanceof backend.academy.scrapper.repository.entities.LinkEntity.StackOverflowInfo stackOverflowInfo) {
            return stackOverflowClient
                .getStackOverflowNewAnswers(stackOverflowInfo.questionId(), lastUpdated)
                .flatMapMany(res -> Flux.fromIterable(res.items()))
                .doOnNext(activity -> log.info("so update {}", activity))
                .flatMap(answer -> botClient.sendUpdate(answer, link))
                .then(Mono.just(true));

        }
        throw new IllegalStateException("Parser doesn't work correctly");
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
