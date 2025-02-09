package backend.academy.scrapper.repository;

import java.util.Optional;

public interface BotStorageRepository {
    Optional<User> saveUser(User user);

    Optional<User> getUser(Long chatId);

    Optional<User> deleteUser(Long chatId);
}
