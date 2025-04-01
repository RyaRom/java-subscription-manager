package backend.academy.scrapper.repository;

import backend.academy.configuration.EnvType;
import backend.academy.scrapper.repository.dto.Link;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InMemoryLinkRepository implements LinkRepository {
    private final EnvType envType;
    private final Map<Long, Link> storage = new HashMap<>();

    @Override
    public Optional<Link> findById(Long linkId) {
        return Optional.ofNullable(storage.get(linkId));
    }

    @Override
    public Optional<Link> findByUrl(String url) {
        return storage.values().stream().filter(l -> l.getUrl().equals(url)).findFirst();
    }

    @Override
    public List<Link> findAll() {
        return storage.values().stream().toList();
    }

    @Override
    public Link save(Link link) {
        if (findByUrl(link.getUrl()).isEmpty()) {
            return storage.put(link.getLinkId(), link);
        }
        return null;
    }

    @Override
    public List<Link> saveAll(List<Link> links) {
        links.forEach(this::save);
        return links;
    }

    @Override
    public Optional<Link> deleteById(Long linkId) {
        return Optional.ofNullable(storage.remove(linkId));
    }

    @Override
    public Optional<Link> deleteByUrl(String url) {
        return findAll().stream()
                .filter(l -> l.getUrl().equals(url))
                .findFirst()
                .map(it -> {
                    storage.remove(it.getLinkId());
                    return it;
                });
    }

    @Override
    public void dropForTest() {
        if (envType != EnvType.TEST) {
            return;
        }
        storage.clear();
    }
}
