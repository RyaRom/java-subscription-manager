package backend.academy.scrapper.service.parsers;

import static backend.academy.scrapper.repository.dto.Link.StackOverflowInfo.parseStackOverflowInfo;

import backend.academy.scrapper.clients.BotClient;
import backend.academy.scrapper.clients.StackOverflowClient;
import backend.academy.scrapper.repository.dto.Link;
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
    public boolean parse(Link link, List<String> tokens) {
        if (tokens.contains("stackoverflow.com")) {
            link.setLinkType(Link.Type.STACK_OVERFLOW);
            link.setStackOverflowInfo(parseStackOverflowInfo(link.getUrl()));
            return true;
        }
        return false;
    }

    @Override
    public Mono<Boolean> update(Link link, Instant lastUpdated) {
        if (link.getLinkType() != Link.Type.STACK_OVERFLOW) {
            return Mono.just(false);
        }
        if (link.getStackOverflowInfo() == null) {
            link.setStackOverflowInfo(parseStackOverflowInfo(link.getUrl()));
        }
        return stackOverflowClient
                .getStackOverflowNewAnswers(link.getStackOverflowInfo().questionId(), lastUpdated)
                .flatMapMany(res -> Flux.fromIterable(res.items()))
                .doOnNext(activity -> log.info("so update {}", activity))
                .flatMap(answer -> botClient.sendUpdate(answer, link))
                .then(Mono.just(true));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
