package backend.academy.bot.repository;

import backend.academy.bot.telegram.utils.fsm.BotState;
import backend.academy.bot.telegram.utils.fsm.DefaultStates;
import org.springframework.stereotype.Repository;

@Repository
public interface BotStateRepository {
    BotState updateState(Long chatId, BotState state);

    BotState getState(Long chatId);

    BotState clearState(Long chatId);

    default BotState getEmptyState() {
        return DefaultStates.NONE;
    }
}
