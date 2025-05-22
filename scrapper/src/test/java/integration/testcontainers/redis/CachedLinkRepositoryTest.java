package integration.testcontainers.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.proto.impl.LinkEntities;
import backend.academy.scrapper.config.DataConnectionProperties;
import backend.academy.scrapper.repository.links.CachedLinkRepository;
import backend.academy.scrapper.repository.links.LinkRepository;
import backend.academy.scrapper.repository.links.dto.LinkType;
import backend.academy.scrapper.repository.links.entities.GithubInfoEntity;
import backend.academy.scrapper.repository.links.entities.LinkEntity;
import backend.academy.scrapper.repository.links.entities.LinkInfoEntity;
import backend.academy.scrapper.repository.links.entities.StackOverflowInfoEntity;
import integration.BaseTestcontainersTest;
import integration.testcontainers.configuration.TestcontainersGenericConfiguration;
import io.lettuce.core.api.async.RedisAsyncCommands;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestcontainersGenericConfiguration.class)
public class CachedLinkRepositoryTest extends BaseTestcontainersTest {
    public static final String URL_MOCK_GITHUB = "https://github.com/test/repo";
    public static final String URL_MOCK_GITHUB_1 = "https://github.com/test/repo1";
    public static final String URL_MOCK_GITHUB_2 = "https://github.com/test/repo2";

    @Autowired
    private DataConnectionProperties dataConnectionProperties;

    @Autowired
    private RedisAsyncCommands<String, LinkEntities.FullLinkProto> redisAsyncCommands;

    @Autowired
    private LinkRepository delegatedRepository;

    private CachedLinkRepository cachedLinkRepository;

    public static @NotNull String redisUrl() {
        return "redis://" + redisContainer.getHost() + ":" + redisContainer.getFirstMappedPort();
    }

    @BeforeEach
    void setUp() {
        delegatedRepository.dropForTest();

        cachedLinkRepository =
                new CachedLinkRepository(dataConnectionProperties, redisAsyncCommands, delegatedRepository);

        redisAsyncCommands.flushall();
    }

    @AfterEach
    void tearDown() {
        delegatedRepository.dropForTest();
        redisAsyncCommands.flushall();
    }

    @Test
    void findByUrl_shouldCacheResults() {
        String url = URL_MOCK_GITHUB;
        LinkEntity expectedLink = createTestLink(url, LinkType.GITHUB);

        delegatedRepository.save(expectedLink);
        Optional<LinkEntity> result1 = cachedLinkRepository.findByUrl(url);

        assertTrue(result1.isPresent());
        assertEquals(url, result1.get().getUrl());
        Optional<LinkEntity> result2 = cachedLinkRepository.findByUrl(url);
        assertTrue(result2.isPresent());
        assertEquals(url, result2.get().getUrl());

        CompletableFuture<LinkEntities.FullLinkProto> future =
                redisAsyncCommands.get(getPrefix(url)).toCompletableFuture();
        LinkEntities.FullLinkProto cachedValue = future.join();
        assertNotNull(cachedValue);
    }

    @Test
    void findByUrl_whenCacheMiss_shouldStoreInCache() {
        String url = URL_MOCK_GITHUB;
        LinkEntity expectedLink = createTestLink(url, LinkType.GITHUB);
        delegatedRepository.save(expectedLink);
        cachedLinkRepository.findByUrl(url);
        CompletableFuture<LinkEntities.FullLinkProto> future =
                redisAsyncCommands.get(getPrefix(url)).toCompletableFuture();
        LinkEntities.FullLinkProto cachedValue = future.join();

        assertNotNull(cachedValue);
        assertEquals(url, cachedValue.getUrl());
        assertEquals(LinkType.GITHUB.ordinal(), cachedValue.getLinkTypeValue());
    }

    @Test
    void save_shouldClearCache() {
        String url = URL_MOCK_GITHUB;
        LinkEntity link = createTestLink(url, LinkType.GITHUB);

        delegatedRepository.save(link);
        cachedLinkRepository.findByUrl(url);
        CompletableFuture<LinkEntities.FullLinkProto> initialFuture =
                redisAsyncCommands.get(getPrefix(url)).toCompletableFuture();
        assertNotNull(initialFuture.join());

        link.setUrl(url);
        cachedLinkRepository.save(link);
        CompletableFuture<LinkEntities.FullLinkProto> future =
                redisAsyncCommands.get(getPrefix(url)).toCompletableFuture();
        LinkEntities.FullLinkProto cachedValue = future.join();
        assertNull(cachedValue);

        Optional<LinkEntity> result = cachedLinkRepository.findByUrl(url);
        assertTrue(result.isPresent());

        CompletableFuture<LinkEntities.FullLinkProto> newFuture =
                redisAsyncCommands.get(getPrefix(url)).toCompletableFuture();
        assertNotNull(newFuture.join());
    }

    private static @NotNull String getPrefix(String url) {
        return "link:url:" + url;
    }

    @Test
    void addChatId_shouldClearCache() {
        String url = URL_MOCK_GITHUB;
        LinkEntity link = createTestLink(url, LinkType.GITHUB);
        Long chatId = 12345L;
        delegatedRepository.save(link);

        cachedLinkRepository.findByUrl(url);

        CompletableFuture<LinkEntities.FullLinkProto> initialFuture =
                redisAsyncCommands.get(getPrefix(url)).toCompletableFuture();
        assertNotNull(initialFuture.join());

        cachedLinkRepository.addChatId(link, chatId);

        CompletableFuture<LinkEntities.FullLinkProto> future =
                redisAsyncCommands.get(getPrefix(url)).toCompletableFuture();
        LinkEntities.FullLinkProto cachedValue = future.join();
        assertNull(cachedValue);

        List<LinkEntity> linksWithChat = delegatedRepository.findWithChatId(chatId);
        assertEquals(1, linksWithChat.size());
        assertEquals(url, linksWithChat.get(0).getUrl());
    }

    @Test
    void saveAll_shouldClearCacheForAllLinks() {
        String url1 = URL_MOCK_GITHUB_1;
        String url2 = URL_MOCK_GITHUB_2;
        LinkEntity link1 = createTestLink(url1, LinkType.GITHUB);
        LinkEntity link2 = createTestLink(url2, LinkType.GITHUB);
        List<LinkEntity> links = List.of(link1, link2);

        delegatedRepository.saveAll(links);
        cachedLinkRepository.findByUrl(url1);
        cachedLinkRepository.findByUrl(url2);

        CompletableFuture<LinkEntities.FullLinkProto> initialFuture1 =
                redisAsyncCommands.get(getPrefix(url1)).toCompletableFuture();
        CompletableFuture<LinkEntities.FullLinkProto> initialFuture2 =
                redisAsyncCommands.get(getPrefix(url2)).toCompletableFuture();
        assertNotNull(initialFuture1.join());
        assertNotNull(initialFuture2.join());
        cachedLinkRepository.saveAll(links);

        CompletableFuture<LinkEntities.FullLinkProto> future1 =
                redisAsyncCommands.get(getPrefix(url1)).toCompletableFuture();
        CompletableFuture<LinkEntities.FullLinkProto> future2 =
                redisAsyncCommands.get(getPrefix(url2)).toCompletableFuture();

        assertNull(future1.join());
        assertNull(future2.join());
    }

    @Test
    void deleteByUrl_shouldClearCache() {
        String url = URL_MOCK_GITHUB;
        LinkEntity link = createTestLink(url, LinkType.GITHUB);

        delegatedRepository.save(link);

        cachedLinkRepository.findByUrl(url);

        CompletableFuture<LinkEntities.FullLinkProto> initialFuture =
                redisAsyncCommands.get(getPrefix(url)).toCompletableFuture();
        assertNotNull(initialFuture.join());

        Optional<LinkEntity> deleted = cachedLinkRepository.deleteByUrl(url);

        assertTrue(deleted.isPresent());
        assertEquals(url, deleted.get().getUrl());

        CompletableFuture<LinkEntities.FullLinkProto> future =
                redisAsyncCommands.get(getPrefix(url)).toCompletableFuture();
        LinkEntities.FullLinkProto cachedValue = future.join();
        assertNull(cachedValue);

        assertTrue(delegatedRepository.findByUrl(url).isEmpty());
    }

    @Test
    void dropForTest_shouldClearRedisCache() {
        String url = URL_MOCK_GITHUB;
        LinkEntity link = createTestLink(url, LinkType.GITHUB);
        delegatedRepository.save(link);
        cachedLinkRepository.findByUrl(url);

        CompletableFuture<LinkEntities.FullLinkProto> future1 =
                redisAsyncCommands.get(getPrefix(url)).toCompletableFuture();
        assertNotNull(future1.join());

        boolean result = cachedLinkRepository.dropForTest();
        assertTrue(result);

        CompletableFuture<LinkEntities.FullLinkProto> future2 =
                redisAsyncCommands.get(getPrefix(url)).toCompletableFuture();
        assertNull(future2.join());
        assertTrue(delegatedRepository.findAll().isEmpty());
    }

    @Test
    void findById_shouldDelegateToRepository() {
        LinkEntity expectedLink = createTestLink(URL_MOCK_GITHUB, LinkType.GITHUB);
        LinkEntity savedLink = delegatedRepository.save(expectedLink);
        Long linkId = savedLink.getLinkId();
        Optional<LinkEntity> result = cachedLinkRepository.findById(linkId);

        assertTrue(result.isPresent());
        assertEquals(linkId, result.get().getLinkId());
    }

    @Test
    void findAll_shouldDelegateToRepository() {
        List<LinkEntity> links = List.of(
                createTestLink(URL_MOCK_GITHUB_1, LinkType.GITHUB), createTestLink(URL_MOCK_GITHUB_2, LinkType.GITHUB));
        delegatedRepository.saveAll(links);
        List<LinkEntity> result = cachedLinkRepository.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void findAllPaginated_shouldDelegateToRepository() {
        List<LinkEntity> links = List.of(
                createTestLink(URL_MOCK_GITHUB_1, LinkType.GITHUB),
                createTestLink(URL_MOCK_GITHUB_2, LinkType.GITHUB),
                createTestLink("https://github.com/test/repo3", LinkType.GITHUB));
        List<LinkEntity> savedLinks = delegatedRepository.saveAll(links);
        long firstLinkId = savedLinks.get(0).getLinkId();
        int limit = 2;
        List<LinkEntity> result = cachedLinkRepository.findAllPaginated(firstLinkId, limit);

        assertEquals(2, result.size());
    }

    @Test
    void findWithChatId_shouldDelegateToRepository() {
        Long chatId = 12345L;
        LinkEntity link1 = createTestLink(URL_MOCK_GITHUB_1, LinkType.GITHUB);
        LinkEntity link2 = createTestLink(URL_MOCK_GITHUB_2, LinkType.GITHUB);

        delegatedRepository.save(link1);
        delegatedRepository.save(link2);
        delegatedRepository.addChatId(link1, chatId);
        delegatedRepository.addChatId(link2, chatId);

        List<LinkEntity> result = cachedLinkRepository.findWithChatId(chatId);
        assertEquals(2, result.size());
    }

    private LinkEntity createTestLink(String url, LinkType type) {
        LinkInfoEntity info;
        if (type == LinkType.GITHUB) {
            info = new GithubInfoEntity("owner", "repo");
        } else {
            info = new StackOverflowInfoEntity(123L);
        }

        LinkEntity link = new LinkEntity();
        link.setUrl(url);
        link.setLinkType(type);
        link.setLinkInfo(info);
        info.setLink(link);
        link.setChatIds(List.of());

        return link;
    }
}
