package backend.academy.scrapper.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryStorage implements BotStorageRepository {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Optional<User> saveUser(User user) {
        return Optional.ofNullable(users.put(user.chatId(), user));
    }

    @Override
    public Optional<User> getUser(Long chatId) {
        return Optional.ofNullable(users.get(chatId));
    }

    @Override
    public Optional<User> deleteUser(Long chatId) {
        return Optional.ofNullable(users.remove(chatId));
    }


}
