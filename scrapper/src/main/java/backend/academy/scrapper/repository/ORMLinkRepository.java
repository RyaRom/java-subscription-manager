package backend.academy.scrapper.repository;

import backend.academy.configuration.EnvType;
import backend.academy.scrapper.repository.dto.LinkDto;
import backend.academy.scrapper.repository.entities.ChatIdEntity;
import backend.academy.scrapper.repository.entities.LinkEntity;
import backend.academy.scrapper.service.mappers.LinkMapper;
import jakarta.persistence.NoResultException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;

@Repository
@Log4j2
@RequiredArgsConstructor
public class ORMLinkRepository implements LinkRepository {
    private final EnvType envType;
    private final SessionFactory sessionFactory;

    public Session openSession() {
        return sessionFactory.openSession();
    }

    @Override
    public Optional<LinkDto> findById(Long linkId) {
        try (Session session = openSession()) {
            return Optional.ofNullable(session.get(LinkEntity.class, linkId))
                .map(LinkMapper::parseToDto);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<LinkDto> findByUrl(String url) {
        try (Session session = openSession()) {
            return Optional.of(
                    session.createQuery("from LinkEntity link where link.url = :url", LinkEntity.class)
                        .setParameter("url", url)
                        .getSingleResult())
                .map(LinkMapper::parseToDto);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<LinkDto> findAll() {
        try (Session session = openSession()) {
            return session.createQuery("from LinkEntity", LinkEntity.class)
                .getResultList()
                .stream()
                .map(LinkMapper::parseToDto)
                .toList();
        }
    }

    @Override
    public @Nullable LinkDto save(LinkDto link) {
        try (Session session = openSession()) {
            try {
                session.getTransaction().begin();
                var entity = LinkMapper.parseToEntity(link);
                if (session.contains(entity)) {
                    session.getTransaction().rollback();
                    return null;
                }
                session.persist(entity);
                session.getTransaction().commit();
            } catch (Exception e) {
                session.getTransaction().rollback();
            }
        }
        return link;
    }

    @Override
    public void addChatId(Long linkId, Long chatId) {
        try (Session session = openSession()) {
            try {
                session.getTransaction().begin();
                var link = Optional.ofNullable(session.get(LinkEntity.class, linkId));
                link.ifPresent(it -> {
                    var newChat = new ChatIdEntity()
                        .setChatId(chatId)
                        .setLink(it);
                    session.persist(newChat);
                    session.getTransaction().commit();
                });
            } catch (Exception e) {
                session.getTransaction().rollback();
            }
        }
    }

    @Override
    public List<LinkDto> saveAll(List<LinkDto> links) {
        try (Session session = openSession()) {
            try {
                session.getTransaction().begin();
                links.forEach(it -> session.persist(LinkMapper.parseToEntity(it)));
                session.getTransaction().commit();
            } catch (Exception e) {
                session.getTransaction().rollback();
            }
        }
        return links;
    }

    @Override
    public Optional<LinkDto> deleteById(Long linkId) {
        try (Session session = openSession()) {
            var deleted = session.getReference(LinkEntity.class, linkId);
            session.remove(deleted);
            return Optional.of(LinkMapper.parseToDto(deleted));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<LinkDto> deleteByUrl(String url) {
        try (Session session = openSession()) {
            var deleted = session.createQuery("from LinkEntity where url = :url", LinkEntity.class)
                .setParameter("url", url)
                .getSingleResult();
            session.remove(deleted);
            return Optional.of(LinkMapper.parseToDto(deleted));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public void dropForTest() {
        try (Session session = openSession()) {
            if (envType != EnvType.TEST) {
                return;
            }
            session.createQuery("delete from LinkEntity", LinkEntity.class).executeUpdate();
        }
    }
}
