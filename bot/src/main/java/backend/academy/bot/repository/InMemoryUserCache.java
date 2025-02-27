package backend.academy.bot.repository;

import backend.academy.bot.telegram.utils.fsm.BotState;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryUserCache implements UserDataCacheRepository {
    private final Map<Long, UserCache> userMap = new HashMap<>();


    @Override
    public void updateState(Long chatId, BotState state) {
        userMap.computeIfAbsent(chatId, k -> UserCache.builder().build())
            .botState(state);
    }

    @Override
    public void updateLink(Long chatId, String link) {
        userMap.computeIfAbsent(chatId, k -> UserCache.builder().build())
            .link(link);
    }

    @Override
    public void updateFilters(Long chatId, String filters) {
        userMap.computeIfAbsent(chatId, k -> UserCache.builder().build())
            .filters(filters);
    }

    @Override
    public void updateTags(Long chatId, String tags) {
        userMap.computeIfAbsent(chatId, k -> UserCache.builder().build())
            .tags(tags);
    }

    @Override
    public UserCache getUser(Long chatId) {
        return userMap.getOrDefault(chatId, UserCache.builder().build());
    }

    @Override
    public void clearUser(Long chatId) {
        userMap.remove(chatId);
    }
}
