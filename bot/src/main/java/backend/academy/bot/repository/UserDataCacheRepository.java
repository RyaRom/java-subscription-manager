package backend.academy.bot.repository;

import backend.academy.bot.telegram.utils.fsm.BotState;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserDataCacheRepository {
    void updateState(Long chatId, BotState state);

    void updateLink(Long chatId, String link);

    void updateFilters(Long chatId, String filters);

    void updateTags(Long chatId, String tags);

    UserCache getUser(Long chatId);

    void clearUser(Long chatId);
}
