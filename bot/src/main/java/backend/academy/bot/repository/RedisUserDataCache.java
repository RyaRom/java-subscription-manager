package backend.academy.bot.repository;

import backend.academy.bot.SubscriptionBotState;
import backend.academy.bot.telegram.sdk.fsm.BotState;
import io.lettuce.core.KeyValue;
import io.lettuce.core.Value;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class RedisUserDataCache implements UserDataCacheRepository {
    private final RedisReactiveCommands<String, String> redisReactiveCommandsString;

    @Override
    public Mono<Void> updateState(Long chatId, BotState state) {
        return redisReactiveCommandsString
            .hset(prefix(chatId), "state", state.toString())
            .then();
    }

    @Override
    public Mono<Void> updateLink(Long chatId, String link) {
        return redisReactiveCommandsString.hset(prefix(chatId), "link", link).then();
    }

    @Override
    public Mono<Void> updateFilters(Long chatId, String filters) {
        return redisReactiveCommandsString
            .hset(prefix(chatId), "filters", filters)
            .then();
    }

    @Override
    public Mono<Void> updateTags(Long chatId, String tags) {
        return redisReactiveCommandsString.hset(prefix(chatId), "tags", tags).then();
    }

    @Override
    public Mono<UserCache> getUser(Long chatId) {
        return redisReactiveCommandsString
            .hgetall(prefix(chatId))
            .collectMap(KeyValue::getKey, Value::getValue)
            .map(map -> UserCache.builder()
                .filters(map.get("filters") == null ? "" : map.get("filters"))
                .tags(map.get("tags") == null ? "" : map.get("tags"))
                .botState(Optional.ofNullable(map.get("state"))
                    .map(SubscriptionBotState::valueOf)
                    .orElse(null))
                .link(map.get("link") == null ? "" : map.get("link"))
                .build()
            );
    }

    @Override
    public Mono<Void> clearUser(Long chatId) {
        return redisReactiveCommandsString.del(prefix(chatId)).then();
    }

    private static String prefix(Long chatId) {
        return "user:" + chatId;
    }
}
