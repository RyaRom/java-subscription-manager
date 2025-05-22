package backend.academy.bot.testcontainers.redis;

import backend.academy.bot.BaseTestcontainersTest;
import backend.academy.bot.SubscriptionBotState;
import backend.academy.bot.repository.RedisUserDataCache;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RedisUserDataCacheTest extends BaseTestcontainersTest {

    @Autowired
    private RedisReactiveCommands<String, String> redisReactiveCommandsString;

    private RedisUserDataCache redisUserDataCache;

    @BeforeEach
    void setUp() {
        redisUserDataCache = new RedisUserDataCache(redisReactiveCommandsString);
        redisReactiveCommandsString.flushall().block();
    }

    @AfterEach
    void tearDown() {
        redisReactiveCommandsString.flushall().block();
    }

    @Test
    void updateState_shouldStoreStateInRedis() {
        Long chatId = 12345L;
        SubscriptionBotState state = SubscriptionBotState.WAITING_FOR_LINK;

        StepVerifier.create(redisUserDataCache.updateState(chatId, state))
            .verifyComplete();

        String key = "user:" + chatId;
        String storedState = redisReactiveCommandsString.hget(key, "state").block();
        assertEquals(state.toString(), storedState);
    }

    @Test
    void updateLink_shouldStoreLinkInRedis() {
        Long chatId = 12345L;
        String link = "https://github.com/test/repo";

        StepVerifier.create(redisUserDataCache.updateLink(chatId, link))
            .verifyComplete();

        String key = "user:" + chatId;
        String storedLink = redisReactiveCommandsString.hget(key, "link").block();
        assertEquals(link, storedLink);
    }

    @Test
    void updateFilters_shouldStoreFiltersInRedis() {
        Long chatId = 12345L;
        String filters = "filter1,filter2";

        StepVerifier.create(redisUserDataCache.updateFilters(chatId, filters))
            .verifyComplete();

        String key = "user:" + chatId;
        String storedFilters = redisReactiveCommandsString.hget(key, "filters").block();
        assertEquals(filters, storedFilters);
    }

    @Test
    void updateTags_shouldStoreTagsInRedis() {
        Long chatId = 12345L;
        String tags = "tag1,tag2";

        StepVerifier.create(redisUserDataCache.updateTags(chatId, tags))
            .verifyComplete();

        String key = "user:" + chatId;
        String storedTags = redisReactiveCommandsString.hget(key, "tags").block();
        assertEquals(tags, storedTags);
    }

    @Test
    void getUser_shouldReturnUserFromRedis() {
        Long chatId = 12345L;
        SubscriptionBotState state = SubscriptionBotState.WAITING_FOR_LINK;
        String link = "https://github.com/test/repo";
        String filters = "filter1,filter2";
        String tags = "tag1,tag2";

        String key = "user:" + chatId;
        redisReactiveCommandsString.hset(key, "state", state.toString()).block();
        redisReactiveCommandsString.hset(key, "link", link).block();
        redisReactiveCommandsString.hset(key, "filters", filters).block();
        redisReactiveCommandsString.hset(key, "tags", tags).block();

        StepVerifier.create(redisUserDataCache.getUser(chatId))
            .assertNext(userCache -> {
                assertNotNull(userCache);
                assertEquals(state, userCache.getBotState());
                assertEquals(link, userCache.getLink());
                assertEquals(filters, userCache.getFilters());
                assertEquals(tags, userCache.getTags());
            })
            .verifyComplete();
    }

    @Test
    void clearUser_shouldRemoveUserFromRedis() {
        Long chatId = 12345L;
        SubscriptionBotState state = SubscriptionBotState.WAITING_FOR_LINK;
        String link = "https://github.com/test/repo";

        String key = "user:" + chatId;
        redisReactiveCommandsString.hset(key, "state", state.toString()).block();
        redisReactiveCommandsString.hset(key, "link", link).block();

        StepVerifier.create(redisUserDataCache.clearUser(chatId))
            .verifyComplete();

        Long exists = redisReactiveCommandsString.exists(key).block();
        assertEquals(0L, exists);
    }

}
