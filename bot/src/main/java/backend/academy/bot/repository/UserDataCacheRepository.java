package backend.academy.bot.repository;

import backend.academy.bot.telegram.utils.fsm.BotState;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserDataCacheRepository {
    Mono<Void> updateState(Long chatId, BotState state);

    Mono<Void> updateLink(Long chatId, String link);

    Mono<Void> updateFilters(Long chatId, String filters);

    Mono<Void> updateTags(Long chatId, String tags);

    Mono<UserCache> getUser(Long chatId);

    Mono<Void> clearUser(Long chatId);
}
