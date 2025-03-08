package backend.academy.scrapper.repository;

import backend.academy.scrapper.repository.dto.Link;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkRepository {
    Optional<Link> find(Long linkId);

    Optional<Link> find(String url);

    List<Link> findAll();

    void save(Link link);

    void save(List<Link> links);

    Optional<Link> delete(Long linkId);

    Optional<Link> delete(String url);

    void drop();
}
