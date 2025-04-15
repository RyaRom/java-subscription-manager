package backend.academy.scrapper.repository;

import backend.academy.scrapper.repository.dto.LinkDto;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkRepository {
    Optional<LinkDto> findById(Long linkId);

    Optional<LinkDto> findByUrl(String url);

    List<LinkDto> findAll();

    @Nullable
    LinkDto save(LinkDto link);

    void addChatId(Long linkId, Long chatId);

    List<LinkDto> saveAll(List<LinkDto> links);

    Optional<LinkDto> deleteById(Long linkId);

    Optional<LinkDto> deleteByUrl(String url);

    void dropForTest();
}
