package backend.academy.scrapper.repository;

import backend.academy.configuration.EnvType;
import backend.academy.scrapper.repository.dto.LinkDto;
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
    private final Map<Long, LinkDto> storage = new HashMap<>();

    @Override
    public Optional<LinkDto> findById(Long linkId) {
        return Optional.ofNullable(storage.get(linkId));
    }

    @Override
    public Optional<LinkDto> findByUrl(String url) {
        return storage.values().stream().filter(l -> l.getUrl().equals(url)).findFirst();
    }

    @Override
    public List<LinkDto> findAll() {
        return storage.values().stream().toList();
    }

    @Override
    public LinkDto save(LinkDto link) {
        if (findByUrl(link.getUrl()).isEmpty()) {
            return storage.put(link.getLinkId(), link);
        }
        return null;
    }

    @Override
    public void addChatId(Long linkId, Long chatId) {
        findById(linkId).ifPresent(link -> link.getChatIds().add(chatId));
    }

    @Override
    public List<LinkDto> saveAll(List<LinkDto> links) {
        links.forEach(this::save);
        return links;
    }

    @Override
    public Optional<LinkDto> deleteById(Long linkId) {
        return Optional.ofNullable(storage.remove(linkId));
    }

    @Override
    public Optional<LinkDto> deleteByUrl(String url) {
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
