package backend.academy.scrapper.repository.links;

import backend.academy.configuration.EnvType;
import backend.academy.scrapper.repository.links.entities.ChatIdEntity;
import backend.academy.scrapper.repository.links.entities.LinkEntity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
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
    public List<LinkEntity> findAllPaginated(long lastId, int limit) {
        return List.of();
    }

    @Override
    public LinkEntity save(LinkEntity link) {
        log.info("saving link {}", link.toString());
        log.info("storage {}", storage);
        if (findByUrl(link.getUrl()).isEmpty()) {
            return storage.put((long) link.getUrl().hashCode(), link);
        }
        return null;
    }

    @Override
    public void addChatId(LinkEntity link, Long chatId) {
        findById(link.getLinkId()).ifPresent(link1 -> link1.getChatIds().add(new ChatIdEntity(chatId, link)));
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
    public boolean dropForTest() {
        if (envType != EnvType.TEST) {
            return false;
        }
        storage.clear();
        return true;
    }

    @Override
    public List<LinkEntity> findWithChatId(Long chatId) {
        log.info("getting links for chat {}, storage {}", chatId, storage);
        return findAll().stream()
                .filter(l -> l.getChatIds().stream().anyMatch(c -> c.getChatId().equals(chatId)))
                .toList();
    }
}
