package backend.academy.bot.testcontainers.redis;

import backend.academy.bot.BaseTestcontainersTest;
import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.clients.ScrapperClientCached;
import backend.academy.bot.clients.ScrapperPublisher;
import backend.academy.bot.clients.ScrapperPublisherCached;
import backend.academy.bot.config.DataProps;
import backend.academy.dto.AddLinkRequest;
import backend.academy.dto.LinkResponse;
import backend.academy.dto.ListLinkResponse;
import backend.academy.proto.impl.Links;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static backend.academy.bot.clients.ScrapperClientCached.prefix;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class CacheRedisTest extends BaseTestcontainersTest {
    @Autowired
    private DataProps dataProps;
    @Autowired
    private RedisReactiveCommands<String, Links.ListLinksProto> redisReactiveCommandsProto;
    private ScrapperClient mockScrapperClient;
    private ScrapperPublisher mockScrapperPublisher;
    private ScrapperClientCached cachedClient;
    private ScrapperPublisherCached scrapperPublisherCached;

    public static @NotNull String redisUrl() {
        return "redis://" + redisContainer.getHost() + ":" + redisContainer.getFirstMappedPort();
    }

    @BeforeEach
    void setUp() {
        mockScrapperClient = Mockito.mock(ScrapperClient.class);
        mockScrapperPublisher = Mockito.mock(ScrapperPublisher.class);
        when(mockScrapperPublisher.addLink(any(), any(AddLinkRequest.class)))
            .thenReturn(Mono.empty());
        when(mockScrapperPublisher.removeLink(any(), any()))
            .thenReturn(Mono.empty());

        cachedClient = new ScrapperClientCached(dataProps, mockScrapperClient, redisReactiveCommandsProto);
        scrapperPublisherCached = new ScrapperPublisherCached(mockScrapperPublisher,
            redisReactiveCommandsProto);

        redisReactiveCommandsProto.flushall().block();
    }

    @AfterEach
    void tearDown() {
        redisReactiveCommandsProto.flushall().block();
    }

    @Test
    void getLinks_shouldCacheResults() {
        Long chatId = 12345L;
        ListLinkResponse expectedResponse = createTestLinkResponse();

        when(mockScrapperClient.getLinks(chatId)).thenReturn(Mono.just(expectedResponse));

        StepVerifier.create(cachedClient.getLinks(chatId))
            .assertNext(response -> {
                assertEquals(expectedResponse.size(), response.size());
                assertEquals(expectedResponse.links().size(), response.links().size());
                assertEquals(expectedResponse.links().get(0).url(), response.links().get(0).url());
            })
            .verifyComplete();

        String key = prefix(chatId);
        Links.ListLinksProto cachedValue = redisReactiveCommandsProto.get(key).block();
        assertNotNull(cachedValue);

        StepVerifier.create(cachedClient.getLinks(chatId))
            .assertNext(response -> {
                assertEquals(expectedResponse.size(), response.size());
                assertEquals(expectedResponse.links().size(), response.links().size());
                assertEquals(expectedResponse.links().get(0).url(), response.links().get(0).url());
            })
            .verifyComplete();

        StepVerifier.create(scrapperPublisherCached.addLink(chatId,
                AddLinkRequest.builder()
                    .link("https://github.com/test/repo")
                    .filters(List.of("filter1", "filter2"))
                    .tags(List.of("tag1", "tag2"))
                    .build()))
            .verifyComplete();

        Links.ListLinksProto emptyCache = redisReactiveCommandsProto.get(key).block();
        assertNull(emptyCache);

        StepVerifier.create(cachedClient.getLinks(chatId))
            .assertNext(response -> {
                assertEquals(expectedResponse.size(), response.size());
                assertEquals(expectedResponse.links().size(), response.links().size());
                assertEquals(expectedResponse.links().get(0).url(), response.links().get(0).url());
            })
            .verifyComplete();

        Links.ListLinksProto cacheAgain = redisReactiveCommandsProto.get(key).block();
        assertNotNull(cacheAgain);

        StepVerifier.create(scrapperPublisherCached.removeLink(chatId, "url"))
            .verifyComplete();

        Links.ListLinksProto emptyCacheAgain = redisReactiveCommandsProto.get(key).block();
        assertNull(emptyCacheAgain);
    }

    @Test
    void getLinks_shouldStoreInCache() {
        Long chatId = 12345L;
        ListLinkResponse expectedResponse = createTestLinkResponse();

        when(mockScrapperClient.getLinks(chatId)).thenReturn(Mono.just(expectedResponse));

        cachedClient.getLinks(chatId).block();

        String key = prefix(chatId);
        Links.ListLinksProto cachedValue = redisReactiveCommandsProto.get(key).block();

        assertNotNull(cachedValue);
        assertEquals(expectedResponse.links().size(), cachedValue.getLinksCount());
        assertEquals(expectedResponse.links().get(0).url(), cachedValue.getLinks(0).getUrl());
    }

    private ListLinkResponse createTestLinkResponse() {
        LinkResponse link1 = new LinkResponse(1L, "https://github.com/test/repo1", List.of("tag1", "tag2"), List.of(
            "filter1"));
        LinkResponse link2 = new LinkResponse(2L, "https://github.com/test/repo2", List.of("tag3"), List.of("filter2"
            , "filter3"));

        return new ListLinkResponse(List.of(link1, link2), 2);
    }
}
