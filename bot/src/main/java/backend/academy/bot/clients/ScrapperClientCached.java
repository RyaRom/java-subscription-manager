package backend.academy.bot.clients;

import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinkResponse;
import backend.academy.proto.impl.Links;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

@Log4j2
@RequiredArgsConstructor
public class ScrapperClientCached implements ScrapperClient {
    private final ScrapperClient scrapperClient;
    private final RedisReactiveCommands<String, Links.ListLinksProto> redisReactiveCommands;

    @Override
    public Mono<ListLinkResponse> getLinks(Long chatId) {
        return redisReactiveCommands.get(prefix(chatId))
            .map(proto -> {
                log.info("getLinks: Using cached links for chat {}", chatId);
                return mapProto(proto);
            })
            .switchIfEmpty(scrapperClient.getLinks(chatId)
                .flatMap(fetched -> {
                    log.info("getLinks: Caching links for chat {}", chatId);
                    return redisReactiveCommands.set(prefix(chatId), mapProto(fetched))
                        .thenReturn(fetched);
                }));
    }

    public static String prefix(Long chatId) {
        return "links:chat:" + chatId;
    }

    private static @NotNull ListLinkResponse mapProto(Links.ListLinksProto proto) {
        var links = proto.getLinksList()
            .stream()
            .map(link -> new LinkResponse(
                link.getLinkId(), link.getUrl(),
                link.getTagsList(), link.getFiltersList()
            )).toList();
        return new ListLinkResponse(
            links, links.size()
        );
    }

    private static @NotNull Links.ListLinksProto mapProto(ListLinkResponse response) {
        return Links.ListLinksProto.newBuilder()
            .addAllLinks(response.links().stream()
                .map(link -> Links.LinkProto.newBuilder()
                    .setLinkId(link.linkId())
                    .setUrl(link.url())
                    .addAllTags(link.tags())
                    .addAllFilters(link.filters())
                    .build())
                .toList())
            .build();
    }
}
