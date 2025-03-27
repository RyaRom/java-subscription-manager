package backend.academy.scrapper.repository;

import backend.academy.scrapper.repository.dto.Link;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkRepository {
    Optional<Link> findById(Long linkId);

    Optional<Link> findByUrl(String url);

    List<Link> findAll();

    @Nullable
    Link save(Link link);

    List<Link> saveAll(List<Link> links);

    Optional<Link> deleteById(Long linkId);

    Optional<Link> deleteByUrl(String url);

    void dropForTest();
}
