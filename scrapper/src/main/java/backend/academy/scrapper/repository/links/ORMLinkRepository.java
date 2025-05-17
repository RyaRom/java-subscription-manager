package backend.academy.scrapper.repository.links;

import backend.academy.configuration.EnvType;
import backend.academy.scrapper.repository.links.entities.ChatIdEntity;
import backend.academy.scrapper.repository.links.entities.LinkEntity;
import jakarta.persistence.NoResultException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jspecify.annotations.Nullable;

@Log4j2
@RequiredArgsConstructor
public class ORMLinkRepository implements LinkRepository {
    private final EnvType envType;
    private final SessionFactory sessionFactory;

    public Session openSession() {
        return sessionFactory.openSession();
    }

    @Override
    public Optional<LinkEntity> findById(Long linkId) {
        try (Session session = openSession()) {
            return Optional.ofNullable(session.get(LinkEntity.class, linkId));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<LinkEntity> findByUrl(String url) {
        try (Session session = openSession()) {
            return Optional.of(session.createQuery("from LinkEntity link where link.url = :url", LinkEntity.class)
                    .setParameter("url", url)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<LinkEntity> findAll() {
        try (Session session = openSession()) {
            return session.createQuery("from LinkEntity", LinkEntity.class).getResultList().stream()
                    .toList();
        }
    }

    @Override
    public List<LinkEntity> findAllPaginated(long lastId, int limit) {
        try (Session session = openSession()) {
            return session.createQuery(
                            "SELECT l FROM LinkEntity l " + "WHERE l.linkId > :lastId " + "ORDER BY l.linkId ASC",
                            LinkEntity.class)
                    .setParameter("lastId", lastId)
                    .setMaxResults(limit)
                    .getResultList();
        }
    }

    @Override
    public @Nullable LinkEntity save(LinkEntity link) {
        try (Session session = openSession()) {
            try {
                session.getTransaction().begin();
                if (session.contains(link)) {
                    session.getTransaction().rollback();
                    return null;
                }
                session.persist(link);
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
                    var newChat = new ChatIdEntity(chatId, it);
                    session.persist(newChat);
                    session.getTransaction().commit();
                });
            } catch (Exception e) {
                session.getTransaction().rollback();
            }
        }
    }

    @Override
    public List<LinkEntity> saveAll(List<LinkEntity> links) {
        try (Session session = openSession()) {
            try {
                session.getTransaction().begin();
                links.forEach(session::persist);
                session.getTransaction().commit();
            } catch (Exception e) {
                session.getTransaction().rollback();
            }
        }
        return links;
    }

    @Override
    public Optional<LinkEntity> deleteByUrl(String url) {
        try (Session session = openSession()) {
            try {
                session.beginTransaction();
                var deleted = session.createQuery("from LinkEntity where url = :url", LinkEntity.class)
                        .setParameter("url", url)
                        .getSingleResult();
                session.remove(deleted);
                session.getTransaction().commit();
                return Optional.of(deleted);
            } catch (Exception e) {
                session.getTransaction().rollback();
            }
        } catch (NoResultException e) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    @Override
    public void dropForTest() {
        try (Session session = openSession()) {
            try {
                if (envType != EnvType.TEST) {
                    return;
                }
                session.beginTransaction();
                session.createNativeQuery("TRUNCATE TABLE link CASCADE").executeUpdate();
                session.getTransaction().commit();
            } catch (Exception e) {
                session.getTransaction().rollback();
            }
        }
    }

    @Override
    public List<LinkEntity> findWithChatId(Long chatId) {
        try (Session session = openSession()) {
            return session.createQuery(
                            "SELECT DISTINCT l FROM LinkEntity l JOIN l.chatIds c WHERE c.chatId = :chatId",
                            LinkEntity.class)
                    .setParameter("chatId", chatId)
                    .getResultList();
        }
    }
}
