package integration.testcontainers.postgres;

import backend.academy.configuration.EnvType;
import backend.academy.scrapper.repository.ORMLinkRepository;
import backend.academy.scrapper.repository.entities.GithubInfoEntity;
import backend.academy.scrapper.repository.entities.LinkInfoEntity;
import backend.academy.scrapper.repository.dto.LinkType;
import backend.academy.scrapper.repository.entities.LinkEntity;
import backend.academy.scrapper.repository.entities.StackOverflowInfoEntity;
import integration.BaseTestcontainersTest;
import java.util.List;
import java.util.Optional;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ORMLinkRepositoryTest extends BaseTestcontainersTest {

    @Autowired
    private SessionFactory sessionFactory;

    private ORMLinkRepository linkRepository;

    @BeforeEach
    void setUp() {
        linkRepository = new ORMLinkRepository(EnvType.TEST, sessionFactory);
        linkRepository.dropForTest();
    }

    @Test
    void findById_existingId_returnsLinkEntityWithAllFields() {
        LinkEntity link = createTestLink();
        linkRepository.save(link);

        Optional<LinkEntity> found = linkRepository.findById(link.getLinkId());

        assertTrue(found.isPresent());
        LinkEntity result = found.get();
        assertEquals(link.getUrl(), result.getUrl());
        assertEquals(link.getLinkType(), result.getLinkType());
        assertEquals(link.getLinkInfo(), result.getLinkInfo());
        assertEquals(link.getChatIds().size(), result.getChatIds().size());
    }

    @Test
    void findByUrl_existingUrl_returnsCompleteLinkEntity() {
        String url = "https://github.com/example/repo";
        LinkEntity link = createTestLink(url);
        linkRepository.save(link);

        Optional<LinkEntity> found = linkRepository.findByUrl(url);

        assertTrue(found.isPresent());
        LinkEntity result = found.get();
        assertEquals(url, result.getUrl());
        assertEquals(LinkType.GITHUB, result.getLinkType());
        assertNotNull(result.getLinkInfo());
    }

    @Test
    void save_persistsAllLinkFieldsCorrectly() {
        LinkEntity link = createTestLink();

        LinkEntity saved = linkRepository.save(link);

        assertNotNull(saved.getLinkId());
        Optional<LinkEntity> found = linkRepository.findById(saved.getLinkId());
        assertTrue(found.isPresent());
        assertEquals(saved.getUrl(), found.get().getUrl());
        assertEquals(saved.getLinkType(), found.get().getLinkType());
        assertEquals(saved.getLinkInfo(), found.get().getLinkInfo());
    }

    @Test
    void addChatId_createsAssociationBetweenLinkAndChat() {
        LinkEntity link = createTestLink();
        linkRepository.save(link);
        Long chatId = 12345L;

        linkRepository.addChatId(link.getLinkId(), chatId);

        List<LinkEntity> linksWithChat = linkRepository.findWithChatId(chatId);
        assertEquals(1, linksWithChat.size());
        assertEquals(link.getLinkId(), linksWithChat.get(0).getLinkId());
        assertEquals(1, linksWithChat.get(0).getChatIds().size());
    }

    @Test
    void findWithChatId_returnsOnlyLinksWithSpecifiedChatId() {
        LinkEntity link1 = createTestLink("https://github.com/repo1");
        LinkEntity link2 = createTestLink("https://github.com/repo2");
        linkRepository.saveAll(List.of(link1, link2));

        Long chatId1 = 11111L;
        Long chatId2 = 22222L;
        linkRepository.addChatId(link1.getLinkId(), chatId1);
        linkRepository.addChatId(link2.getLinkId(), chatId2);

        List<LinkEntity> results = linkRepository.findWithChatId(chatId1);

        assertEquals(1, results.size());
        assertEquals(link1.getLinkId(), results.get(0).getLinkId());
    }

    @Test
    void deleteByUrl_removesLinkAndAssociatedChatIds() {
        String url = "https://github.com/to/delete";
        LinkEntity link = createTestLink(url);
        linkRepository.save(link);
        linkRepository.addChatId(link.getLinkId(), 99999L);

        Optional<LinkEntity> deleted = linkRepository.deleteByUrl(url);

        assertTrue(deleted.isPresent());
        assertEquals(url, deleted.get().getUrl());
        assertTrue(linkRepository.findByUrl(url).isEmpty());

        List<LinkEntity> linksWithChat = linkRepository.findWithChatId(99999L);
        assertTrue(linksWithChat.isEmpty());
    }

    @Test
    void saveAll_persistsMultipleLinksWithDifferentTypes() {
        LinkEntity githubLink = createTestLink("https://github.com/repo1", LinkType.GITHUB);
        LinkEntity stackoverflowLink = createTestLink("https://stackoverflow.com/questions/123",
            LinkType.STACK_OVERFLOW);

        linkRepository.saveAll(List.of(githubLink, stackoverflowLink));

        List<LinkEntity> allLinks = linkRepository.findAll();
        assertEquals(2, allLinks.size());
        assertTrue(allLinks.stream().anyMatch(l -> l.getLinkType() == LinkType.GITHUB));
        assertTrue(allLinks.stream().anyMatch(l -> l.getLinkType() == LinkType.STACK_OVERFLOW));
    }

    private LinkEntity createTestLink() {
        return createTestLink("https://github.com/example/repo", LinkType.GITHUB);
    }

    private LinkEntity createTestLink(String url) {
        return createTestLink(url, LinkType.GITHUB);
    }

    private LinkEntity createTestLink(String url, LinkType type) {
        LinkInfoEntity info;
        if (type == LinkType.GITHUB) {
            info = new GithubInfoEntity("own", "rp");
        } else {
            info = new StackOverflowInfoEntity(5L);
        }
        LinkEntity link = new LinkEntity();
        info.setLink(link);
        link.setUrl(url);
        link.setLinkType(type);
        link.setLinkInfo(info);
        link.setChatIds(List.of());
        return link;
    }
}
