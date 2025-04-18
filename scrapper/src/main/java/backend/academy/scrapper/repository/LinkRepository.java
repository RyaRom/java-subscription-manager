package backend.academy.scrapper.repository;

import backend.academy.scrapper.repository.entities.LinkEntity;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

public interface LinkRepository {
    Optional<LinkEntity> findById(Long linkId);

    Optional<LinkEntity> findByUrl(String url);

    List<LinkEntity> findAll();

    @Nullable
    LinkEntity save(LinkEntity link);

    void addChatId(Long linkId, Long chatId);

    List<LinkEntity> saveAll(List<LinkEntity> links);

    Optional<LinkEntity> deleteByUrl(String url);

    void dropForTest();

    List<LinkEntity> findWithChatId(Long chatId);
}
