package backend.academy.scrapper.repository.links;

import backend.academy.configuration.EnvType;
import backend.academy.scrapper.repository.links.dto.LinkType;
import backend.academy.scrapper.repository.links.entities.ChatIdEntity;
import backend.academy.scrapper.repository.links.entities.GithubInfoEntity;
import backend.academy.scrapper.repository.links.entities.LinkEntity;
import backend.academy.scrapper.repository.links.entities.StackOverflowInfoEntity;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.Nullable;

@Log4j2
@RequiredArgsConstructor
public class SQLLinkRepository implements LinkRepository {
    private final EnvType envType;
    private final String url;
    private final String username;
    private final String password;

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(
            url, username, password
        );
    }

    @Override
    public Optional<LinkEntity> findById(Long linkId) {
        String query = "SELECT l.*, gi.owner, gi.repo, soi.question_id "
            + "FROM link l "
            + "LEFT JOIN github_info gi ON l.link_id = gi.id "
            + "LEFT JOIN stack_overflow_info soi ON l.link_id = soi.id "
            + "WHERE l.link_id = ?";
        try (var connection = openConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setLong(1, linkId);
                ResultSet rs = statement.executeQuery();
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapLinkEntity(rs));
            }
        } catch (SQLException e) {
            log.error("Error finding link by ID: {}", linkId, e);
            return Optional.empty();
        }
    }

    private LinkEntity mapLinkEntity(ResultSet rs) throws SQLException {
        LinkEntity link = new LinkEntity();
        link.setLinkId(rs.getLong("link_id"));
        link.setUrl(rs.getString("url"));
        link.setLinkType(LinkType.values()[rs.getInt("link_type")]);

        String owner = rs.getString("owner");
        if (!rs.wasNull()) {
            GithubInfoEntity githubInfo = new GithubInfoEntity();
            githubInfo.setOwner(owner);
            githubInfo.setRepo(rs.getString("repo"));
            link.setLinkInfo(githubInfo);
        }

        long questionId = rs.getLong("question_id");
        if (!rs.wasNull()) {
            StackOverflowInfoEntity stackInfo = new StackOverflowInfoEntity();
            stackInfo.setQuestionId(questionId);
            link.setLinkInfo(stackInfo);
        }

        link.setChatIds(findChatIdsForLink(link));
        return link;
    }

    private List<ChatIdEntity> findChatIdsForLink(LinkEntity link) {
        String query = "SELECT chat_id FROM chat WHERE link_id = ?";
        List<ChatIdEntity> chatIds = new ArrayList<>();
        try (var connection = openConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setLong(1, link.getLinkId());
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    chatIds.add(new ChatIdEntity(
                        rs.getLong("chat_id"),
                        link
                    ));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding chats for link: {}", link.getLinkId(), e);
            return List.of();
        }
        return chatIds;
    }


    @Override
    public Optional<LinkEntity> findByUrl(String url) {
        String query = "SELECT l.*, gi.owner, gi.repo, soi.question_id "
            + "FROM link l "
            + "LEFT JOIN github_info gi ON l.link_id = gi.id "
            + "LEFT JOIN stack_overflow_info soi ON l.link_id = soi.id "
            + "WHERE l.url = ?";
        try (var connection = openConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, url);
                ResultSet rs = statement.executeQuery();
                return rs.next() ? Optional.of(mapLinkEntity(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            log.error("Error finding link by URL: {}", url, e);
            return Optional.empty();
        }
    }


    @Override
    public List<LinkEntity> findAll() {
        String query = "SELECT l.*, gi.owner, gi.repo, soi.question_id "
            + "FROM link l "
            + "LEFT JOIN github_info gi ON l.link_id = gi.id "
            + "LEFT JOIN stack_overflow_info soi ON l.link_id = soi.id";
        List<LinkEntity> links = new ArrayList<>();
        try (var connection = openConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    links.add(mapLinkEntity(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error fetching all links", e);
            return List.of();
        }
        return links;
    }

    @Override
    public List<LinkEntity> findAllPaginated(long lastId, int limit) {
        String query = "SELECT l.*, gi.owner, gi.repo, soi.question_id "
            + "FROM link l "
            + "LEFT JOIN github_info gi ON l.link_id = gi.id "
            + "LEFT JOIN stack_overflow_info soi ON l.link_id = soi.id "
            + "WHERE l.link_id > ? ORDER BY l.link_id ASC "
            + "LIMIT ?";
        List<LinkEntity> links = new ArrayList<>();
        try (var connection = openConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setLong(1, lastId);
                statement.setInt(2, limit);
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    links.add(mapLinkEntity(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error fetching all links", e);
            return List.of();
        }
        return links;
    }

    @Override
    public @Nullable LinkEntity save(LinkEntity link) {
        String insertLink = "INSERT INTO link (url, link_type) VALUES (?, ?)";
        String insertChat = "INSERT INTO chat (link_id, chat_id) VALUES (?, ?)";
        String insertStackInfo = "INSERT INTO stack_overflow_info " +
            "(id, question_id) VALUES (?, ?)";
        String insertGithubInfo = "INSERT INTO github_info " +
            "(id, owner, repo) VALUES (?, ?, ?)";
        String insertInfo = "INSERT INTO link_info (id) VALUES (?)";
        try (var connection = openConnection()) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            try (
                PreparedStatement insertLinkQuery = connection.prepareStatement(insertLink,
                    Statement.RETURN_GENERATED_KEYS);
                PreparedStatement insertChatQuery = connection.prepareStatement(insertChat);
                PreparedStatement insertInfoQuery = connection.prepareStatement(insertInfo);
                PreparedStatement insertStackInfoQuery = connection.prepareStatement(insertStackInfo);
                PreparedStatement githubInfoQuery = connection.prepareStatement(insertGithubInfo);
            ) {
                insertLinkQuery.setString(1, link.getUrl());
                insertLinkQuery.setLong(2, link.getLinkType().ordinal());
                insertLinkQuery.executeUpdate();
                ResultSet keys = insertLinkQuery.getGeneratedKeys();
                if (!keys.next()) {
                    throw new IllegalStateException();
                }
                long linkId = keys.getLong(1);
                link.setLinkId(linkId);
                for (var chat : link.getChatIds()) {
                    insertChatQuery.setLong(1, linkId);
                    insertChatQuery.setLong(2, chat.getChatId());
                    insertChatQuery.executeUpdate();
                }
                insertInfoQuery.setLong(1, linkId);
                insertInfoQuery.executeUpdate();
                saveLinkInfo(link, githubInfoQuery, linkId, insertStackInfoQuery);

                connection.commit();
            } catch (Exception e) {
                log.error("Error in transaction save");
                connection.rollback();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            log.error("Error in connection save");
            throw new RuntimeException(e);
        }
        return link;
    }

    private static void saveLinkInfo(LinkEntity link, PreparedStatement githubInfoQuery, long linkId,
                                     PreparedStatement insertStackInfoQuery) throws SQLException {
        switch (link.getLinkType()) {
            case GITHUB -> {
                GithubInfoEntity githubInfo = (GithubInfoEntity) link.getLinkInfo();
                githubInfoQuery.setLong(1, linkId);
                githubInfoQuery.setString(2, githubInfo.getOwner());
                githubInfoQuery.setString(3, githubInfo.getRepo());
                githubInfoQuery.executeUpdate();
            }
            case STACK_OVERFLOW -> {
                StackOverflowInfoEntity stackOverflowInfoEntity =
                    (StackOverflowInfoEntity) link.getLinkInfo();
                insertStackInfoQuery.setLong(1, linkId);
                insertStackInfoQuery.setLong(2, stackOverflowInfoEntity.getQuestionId());
                insertStackInfoQuery.executeUpdate();
            }
        }
    }

    @Override
    public void addChatId(Long linkId, Long chatId) {
        String query = "INSERT INTO chat (link_id, chat_id) VALUES (?, ?)";
        try (var connection = openConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setLong(1, linkId);
                statement.setLong(2, chatId);
                int affectedRows = statement.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Failed to add chat ID");
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
            }
        } catch (SQLException e) {
            log.error("Error adding chat ID {} to link {}", chatId, linkId, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<LinkEntity> saveAll(List<LinkEntity> links) {
        links.forEach(this::save);
        return links;
    }

    @Override
    public Optional<LinkEntity> deleteByUrl(String url) {
        return findByUrl(url).map(link -> {
            String deleteQuery = "DELETE FROM link WHERE url = ?";
            String deleteChatsQuery = "DELETE FROM chat WHERE link_id = ?";
            String deleteInfoQuery = "DELETE FROM link_info WHERE id = ?";
            String deleteSoQuery = "DELETE FROM stack_overflow_info WHERE id = ?";
            String deleteGhQuery = "DELETE FROM github_info WHERE id = ?";
            try (var connection = openConnection()) {
                connection.setAutoCommit(false);
                try (
                    PreparedStatement deleteLink = connection.prepareStatement(deleteQuery);
                    PreparedStatement deleteChats = connection.prepareStatement(deleteChatsQuery);
                    PreparedStatement deleteInfo = connection.prepareStatement(deleteInfoQuery);
                    PreparedStatement deleteSo = connection.prepareStatement(deleteSoQuery);
                    PreparedStatement deleteGh = connection.prepareStatement(deleteGhQuery);
                ) {
                    deleteChats.setLong(1, link.getLinkId());
                    deleteChats.executeUpdate();
                    deleteGh.setLong(1, link.getLinkId());
                    deleteGh.executeUpdate();
                    deleteSo.setLong(1, link.getLinkId());
                    deleteSo.executeUpdate();

                    deleteInfo.setLong(1, link.getLinkId());
                    deleteInfo.executeUpdate();

                    deleteLink.setString(1, url);
                    int affected = deleteLink.executeUpdate();
                    if (affected > 0) {
                        connection.commit();
                        return link;
                    }
                    connection.commit();
                    return null;
                } catch (Exception e) {
                    connection.rollback();
                    return null;
                }
            } catch (SQLException e) {
                log.error("Error deleting link by URL: {}", url, e);
                return null;
            }
        });
    }

    @Override
    public void dropForTest() {
        if (envType != EnvType.TEST) {
            throw new IllegalStateException("Drop operation only allowed in test environment");
        }
        try (var connection = openConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("DELETE FROM chat");
                statement.executeUpdate("DELETE FROM github_info");
                statement.executeUpdate("DELETE FROM stack_overflow_info");
                statement.executeUpdate("DELETE FROM link_info");
                statement.executeUpdate("DELETE FROM link");
            }
        } catch (SQLException e) {
            log.error("Error cleaning test data", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<LinkEntity> findWithChatId(Long chatId) {
        String query = "SELECT l.*, gi.owner, gi.repo, soi.question_id "
            + "FROM link l "
            + "JOIN chat c ON l.link_id = c.link_id "
            + "LEFT JOIN github_info gi ON l.link_id = gi.id "
            + "LEFT JOIN stack_overflow_info soi ON l.link_id = soi.id "
            + "WHERE c.chat_id = ?";

        List<LinkEntity> links = new ArrayList<>();
        try (var connection = openConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setLong(1, chatId);
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    links.add(mapLinkEntity(rs)); // Direct mapping, no extra queries
                }
            }
        } catch (SQLException e) {
            log.error("Error finding links with chat ID: {}", chatId, e);
            return List.of();
        }
        return links;
    }
}
