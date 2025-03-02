package backend.academy.bot.repository;

import backend.academy.bot.telegram.utils.fsm.BotState;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class InMemoryUserCache implements UserDataCacheRepository {
    private final Map<Long, UserCache> userMap = new HashMap<>();


    @Override
    public Mono<Void> updateState(Long chatId, BotState state) {
        userMap.computeIfAbsent(chatId, k -> UserCache.builder().build())
            .botState(state);
        return Mono.empty();
    }

    @Override
    public Mono<Void> updateLink(Long chatId, String link) {
        userMap.computeIfAbsent(chatId, k -> UserCache.builder().build())
            .link(link);
        return Mono.empty();
    }

    @Override
    public Mono<Void> updateFilters(Long chatId, String filters) {
        userMap.computeIfAbsent(chatId, k -> UserCache.builder().build())
            .filters(filters);
        return Mono.empty();
    }

    @Override
    public Mono<Void> updateTags(Long chatId, String tags) {
        userMap.computeIfAbsent(chatId, k -> UserCache.builder().build())
            .tags(tags);
        return Mono.empty();
    }

    @Override
    public Mono<UserCache> getUser(Long chatId) {
        return Mono.just(userMap.getOrDefault(chatId, UserCache.builder().build()));
    }

    @Override
    public Mono<Void> clearUser(Long chatId) {
        userMap.remove(chatId);
        return Mono.empty();
    }
}
