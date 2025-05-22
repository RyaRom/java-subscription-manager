package backend.academy.bot.clients;

import backend.academy.bot.config.DataProps;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinkResponse;
import backend.academy.proto.impl.Links;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;
import static io.lettuce.core.SetArgs.Builder.ex;

@Log4j2
@RequiredArgsConstructor
public class ScrapperClientCached implements ScrapperClient {
    private final DataProps dataProps;
    private final ScrapperClient delegated;
    private final RedisReactiveCommands<String, Links.ListLinksProto> redisReactiveCommandsProto;

    @Override
    public Mono<ListLinkResponse> getLinks(Long chatId) {
        return redisReactiveCommandsProto.get(prefix(chatId))
            .map(proto -> {
                log.info("getLinks: Using cached links for chat {}", chatId);
                return mapProto(proto);
            })
            .switchIfEmpty(delegated.getLinks(chatId)
                .flatMap(fetched -> {
                    log.info("getLinks: Caching links for chat {}", chatId);
                    return redisReactiveCommandsProto.set(prefix(chatId), mapProto(fetched),
                            ex(dataProps.redisExMs()))
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
