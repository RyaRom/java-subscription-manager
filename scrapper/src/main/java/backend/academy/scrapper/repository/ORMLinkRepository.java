package backend.academy.scrapper.repository;

import backend.academy.configuration.EnvType;
import backend.academy.scrapper.repository.dto.LinkDto;
import backend.academy.scrapper.repository.entities.ChatIdEntity;
import backend.academy.scrapper.repository.entities.LinkEntity;
import backend.academy.scrapper.service.mappers.LinkMapper;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;
import org.jspecify.annotations.Nullable;

@Log4j2
@RequiredArgsConstructor
public class ORMLinkRepository implements LinkRepository {
    private final Session session;
    private final EnvType envType;

    @Override
    public Optional<LinkDto> findById(Long linkId) {
        return Optional.ofNullable(session.get(LinkEntity.class, linkId))
            .map(LinkMapper::parseToDto);
    }

    @Override
    public Optional<LinkDto> findByUrl(String url) {
        return Optional.ofNullable(
                session.createQuery("from LinkEntity where url = :url", LinkEntity.class)
                    .setParameter("url", url)
                    .getSingleResult())
            .map(LinkMapper::parseToDto);
    }

    @Override
    public List<LinkDto> findAll() {
        return session.createQuery("from LinkEntity", LinkEntity.class)
            .getResultList()
            .stream()
            .map(LinkMapper::parseToDto)
            .toList();
    }

    @Override
    public @Nullable LinkDto save(LinkDto link) {
        session.persist(LinkMapper.parseToEntity(link));
        return link;
    }

    @Override
    public void addChatId(Long linkId, Long chatId) {
        var link = Optional.ofNullable(session.get(LinkEntity.class, linkId));
        link.ifPresent(it -> {
            var newChat = new ChatIdEntity()
                .setChatId(chatId)
                .setLink(it);
            session.persist(newChat);
        });
    }

    @Override
    public List<LinkDto> saveAll(List<LinkDto> links) {
        links.forEach(it -> session.persist(LinkMapper.parseToEntity(it)));
        return links;
    }

    @Override
    public Optional<LinkDto> deleteById(Long linkId) {
        var deleted = session.getReference(LinkEntity.class, linkId);
        session.remove(deleted);
        return Optional.of(LinkMapper.parseToDto(deleted));
    }

    @Override
    public Optional<LinkDto> deleteByUrl(String url) {
        var deleted = session.createQuery("from LinkEntity where url = :url", LinkEntity.class)
            .setParameter("url", url)
            .getSingleResult();
        session.remove(deleted);
        return Optional.of(LinkMapper.parseToDto(deleted));
    }

    @Override
    public void dropForTest() {
        if (envType != EnvType.TEST) {
            return;
        }
        session.createQuery("delete from LinkEntity", LinkEntity.class).executeUpdate();
    }

    @PreDestroy
    public void close() {
        log.info("Closing session in orm repo");
        session.flush();
        session.close();
    }
}
