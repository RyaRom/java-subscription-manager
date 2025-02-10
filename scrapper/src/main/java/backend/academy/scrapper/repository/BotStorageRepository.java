package backend.academy.scrapper.repository;

import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface BotStorageRepository {
    Optional<User> saveUser(User user);

    Optional<User> getUser(Long chatId);

    Optional<User> deleteUser(Long chatId);
}
