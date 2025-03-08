package backend.academy.scrapper.repository;

import backend.academy.scrapper.repository.dto.Link;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryLinkRepository implements LinkRepository {
    private final Map<Long, Link> storage = new HashMap<>();

    @Override
    public Optional<Link> find(Long linkId) {
        return Optional.ofNullable(storage.get(linkId));
    }

    @Override
    public Optional<Link> find(String url) {
        return storage.values().stream()
            .filter(l -> l.getUrl().equals(url))
            .findFirst();
    }

    @Override
    public List<Link> findAll() {
        return storage.values().stream().toList();
    }

    @Override
    public void save(Link link) {
        storage.put(link.getLinkId(), link);
    }

    @Override
    public void save(List<Link> links) {
        links.forEach(this::save);
    }

    @Override
    public Optional<Link> delete(Long linkId) {
        return Optional.ofNullable(storage.remove(linkId));
    }

    @Override
    public Optional<Link> delete(String url) {
        var link = findAll().stream()
            .filter(l -> l.getUrl().equals(url))
            .findFirst();
        if (link.isEmpty()) {
            return Optional.empty();
        }

        storage.remove(link.get().getLinkId());
        return link;
    }

    @Override
    public void drop() {
        storage.clear();
    }
}
