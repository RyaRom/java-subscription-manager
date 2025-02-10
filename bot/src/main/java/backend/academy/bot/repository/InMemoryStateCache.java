package backend.academy.bot.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryStateCache implements BotStateRepository {
    private final Map<Long, BotState> userStates = new HashMap<>();

    @Override
    public Optional<BotState> updateState(Long chatId, BotState state) {
        return Optional.ofNullable(userStates.put(chatId, state));
    }

    @Override
    public Optional<BotState> getState(Long chatId) {
        return Optional.ofNullable(userStates.get(chatId));
    }

    @Override
    public Optional<BotState> clearState(Long chatId) {
        return Optional.ofNullable(userStates.remove(chatId));
    }
}
