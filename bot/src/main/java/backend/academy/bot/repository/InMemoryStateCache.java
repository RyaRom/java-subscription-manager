package backend.academy.bot.repository;

import backend.academy.bot.telegram.utils.fsm.BotState;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryStateCache implements BotStateRepository {
    private final Map<Long, BotState> userStates = new HashMap<>();

    @Override
    public BotState updateState(Long chatId, BotState state) {
        return Optional.ofNullable(userStates.put(chatId, state)).orElse(getEmptyState());
    }

    @Override
    public BotState getState(Long chatId) {
        return Optional.ofNullable(userStates.get(chatId)).orElse(getEmptyState());
    }

    @Override
    public BotState clearState(Long chatId) {
        return Optional.ofNullable(userStates.remove(chatId)).orElse(getEmptyState());
    }
}
