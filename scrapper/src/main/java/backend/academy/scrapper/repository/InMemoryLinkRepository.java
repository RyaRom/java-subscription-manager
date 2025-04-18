package backend.academy.scrapper.repository;

import backend.academy.configuration.EnvType;
import backend.academy.scrapper.repository.entities.ChatIdEntity;
import backend.academy.scrapper.repository.entities.LinkEntity;
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
    private final Map<Long, LinkEntity> storage = new HashMap<>();

    @Override
    public Optional<LinkEntity> findById(Long linkId) {
        return Optional.ofNullable(storage.get(linkId));
    }

    @Override
    public Optional<LinkEntity> findByUrl(String url) {
        return storage.values().stream().filter(l -> l.getUrl().equals(url)).findFirst();
    }

    @Override
    public List<LinkEntity> findAll() {
        return storage.values().stream().toList();
    }

    @Override
    public LinkEntity save(LinkEntity link) {
        if (findByUrl(link.getUrl()).isEmpty()) {
            return storage.put(link.getLinkId(), link);
        }
        return null;
    }

    @Override
    public void addChatId(Long linkId, Long chatId) {
        findById(linkId).ifPresent(link -> link.getChatIds().add(new ChatIdEntity(chatId, link)));
    }

    @Override
    public List<LinkEntity> saveAll(List<LinkEntity> links) {
        links.forEach(this::save);
        return links;
    }

    @Override
    public Optional<LinkEntity> deleteByUrl(String url) {
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

    @Override
    public List<LinkEntity> findWithChatId(Long chatId) {
        return findAll().stream()
            .filter(l -> l.getChatIds()
                .stream()
                .anyMatch(c -> c.getChatId().equals(chatId)))
            .toList();
    }
}
