package backend.academy.scrapper.repository;

import backend.academy.scrapper.repository.dto.Link;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkRepository {
    Optional<Link> find(Long linkId);

    List<Link> findAll();

    void save(Link link);

    void delete(Long linkId);
}
