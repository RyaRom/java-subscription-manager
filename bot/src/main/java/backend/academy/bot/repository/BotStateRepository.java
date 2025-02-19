package backend.academy.bot.repository;

import backend.academy.bot.telegram.utils.fsm.BotState;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BotStateRepository {
    Optional<BotState> updateState(Long chatId, BotState state);

    Optional<BotState> getState(Long chatId);

    Optional<BotState> clearState(Long chatId);
}
