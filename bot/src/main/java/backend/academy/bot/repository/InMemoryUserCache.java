package backend.academy.bot.repository;

import backend.academy.bot.telegram.utils.fsm.BotState;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class InMemoryUserCache implements UserDataCacheRepository {
    private final Map<Long, UserCache> userMap = new HashMap<>();
    private final Map<Long, SubscriptionCache> subscriptionCacheMap = new HashMap<>();


    @Override
    public Mono<Void> updateState(Long chatId, BotState state) {
        return Mono.fromRunnable(() -> {
            userMap.computeIfAbsent(chatId, k -> UserCache.builder().build())
                .botState(state);
        });
    }

    @Override
    public Mono<Void> updateLink(Long chatId, String link) {
        return Mono.fromRunnable(() -> {
            userMap.computeIfAbsent(chatId, k -> UserCache.builder().build())
                .link(link);
        });
    }

    @Override
    public Mono<Void> updateFilters(Long chatId, String filters) {
        return Mono.fromRunnable(() -> {
            userMap.computeIfAbsent(chatId, k -> UserCache.builder().build())
                .filters(filters);
        });
    }

    @Override
    public Mono<Void> updateTags(Long chatId, String tags) {
        return Mono.fromRunnable(() -> {
            userMap.computeIfAbsent(chatId, k -> UserCache.builder().build())
                .tags(tags);
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

    @Override
    public Mono<SubscriptionCache> getSubscription(Long chatId) {
        return Mono.just(subscriptionCacheMap.getOrDefault(chatId, SubscriptionCache.builder().build()));
    }

    @Override
    public Mono<Void> updateSubscription(Long chatId, SubscriptionCache subscription) {
        return Mono.fromRunnable(() -> {
            subscriptionCacheMap.put(chatId, subscription);
        });
    }

    @Override
    public Mono<Void> clearSubscription(Long chatId) {
        return Mono.fromRunnable(() -> {
            subscriptionCacheMap.remove(chatId);
        });
    }
}
