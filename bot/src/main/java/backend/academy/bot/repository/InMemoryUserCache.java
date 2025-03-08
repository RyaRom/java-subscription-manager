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
        return Mono.fromRunnable(() -> {
            userMap.computeIfAbsent(chatId, k -> UserCache.builder().build()).setBotState(state);
        });
    }

    @Override
    public Mono<Void> updateLink(Long chatId, String link) {
        return Mono.fromRunnable(() -> {
            userMap.computeIfAbsent(chatId, k -> UserCache.builder().build()).setLink(link);
        });
    }

    @Override
    public Mono<Void> updateFilters(Long chatId, String filters) {
        return Mono.fromRunnable(() -> {
            userMap.computeIfAbsent(chatId, k -> UserCache.builder().build()).setFilters(filters);
        });
    }

    @Override
    public Mono<Void> updateTags(Long chatId, String tags) {
        return Mono.fromRunnable(() -> {
            userMap.computeIfAbsent(chatId, k -> UserCache.builder().build()).setTags(tags);
        });
    }

    @Override
    public Mono<UserCache> getUser(Long chatId) {
        return Mono.just(userMap.getOrDefault(chatId, UserCache.builder().build()));
    }

    @Override
    public Mono<Void> clearUser(Long chatId) {
        return Mono.fromRunnable(() -> {
            userMap.remove(chatId);
        });
    }
}
