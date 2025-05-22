package backend.academy.bot.clients;

import backend.academy.dto.AddLinkRequest;
import backend.academy.proto.impl.Links;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Log4j2
@RequiredArgsConstructor
public class ScrapperPublisherCached implements ScrapperPublisher {
    private final ScrapperPublisher delegated;
    private final RedisReactiveCommands<String, Links.ListLinksProto> redisReactiveCommands;

    @Override
    public Mono<Void> removeLink(Long chatId, String link) {
        log.info("removeLink: Removing link from cache for chat {}", chatId);
        return redisReactiveCommands.del(ScrapperClientCached.prefix(chatId))
            .then(delegated.removeLink(chatId, link));
    }

    @Override
    public Mono<Void> registerChat(Long chatId) {
        return delegated.registerChat(chatId);
    }

    @Override
    public Mono<Void> addLink(Long chatId, AddLinkRequest addLinkRequest) {
        log.info("addLink: Removing link from cache for chat {}", chatId);
        return redisReactiveCommands.del(ScrapperClientCached.prefix(chatId))
            .then(delegated.addLink(chatId, addLinkRequest));
    }
}
