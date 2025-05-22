package integration.testcontainers.postgres;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.scrapper.repository.links.LinkRepository;
import backend.academy.scrapper.repository.links.dto.LinkType;
import backend.academy.scrapper.repository.links.entities.GithubInfoEntity;
import backend.academy.scrapper.repository.links.entities.LinkEntity;
import backend.academy.scrapper.repository.links.entities.LinkInfoEntity;
import backend.academy.scrapper.repository.links.entities.StackOverflowInfoEntity;
import integration.BaseTestcontainersTest;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class LinkRepositoryTest extends BaseTestcontainersTest {

    protected abstract LinkRepository getLinkRepository();

    @BeforeEach
    void setUp() {
        getLinkRepository().dropForTest();
    }

    @Test
    void findById_existingId_returnsLinkEntityWithAllFields() {
        LinkEntity link = createTestLink();
        getLinkRepository().save(link);

        Optional<LinkEntity> found = getLinkRepository().findById(link.getLinkId());

        assertTrue(found.isPresent());
        LinkEntity result = found.orElseThrow();
        assertEquals(link.getUrl(), result.getUrl());
        assertEquals(link.getLinkType(), result.getLinkType());
        assertEquals(link.getLinkInfo(), result.getLinkInfo());
        assertEquals(link.getChatIds().size(), result.getChatIds().size());
    }

    @Test
    void findByUrl_existingUrl_returnsCompleteLinkEntity() {
        String url = "https://github.com/example/repo";
        LinkEntity link = createTestLink(url);
        getLinkRepository().save(link);

        Optional<LinkEntity> found = getLinkRepository().findByUrl(url);

        assertTrue(found.isPresent());
        LinkEntity result = found.orElseThrow();
        assertEquals(url, result.getUrl());
        assertEquals(LinkType.GITHUB, result.getLinkType());
        assertNotNull(result.getLinkInfo());
    }

    @Test
    void save_persistsAllLinkFieldsCorrectly() {
        LinkEntity link = createTestLink();

        LinkEntity saved = getLinkRepository().save(link);

        assertNotNull(saved.getLinkId());
        Optional<LinkEntity> found = getLinkRepository().findById(saved.getLinkId());
        assertTrue(found.isPresent());
        assertEquals(saved.getUrl(), found.orElseThrow().getUrl());
        assertEquals(saved.getLinkType(), found.orElseThrow().getLinkType());
        assertEquals(saved.getLinkInfo(), found.orElseThrow().getLinkInfo());
    }

    @Test
    void addChatId_createsAssociationBetweenLinkAndChat() {
        LinkEntity link = createTestLink();
        getLinkRepository().save(link);
        Long chatId = 12345L;

        getLinkRepository().addChatId(link, chatId);

        List<LinkEntity> linksWithChat = getLinkRepository().findWithChatId(chatId);
        assertEquals(1, linksWithChat.size());
        assertEquals(link.getLinkId(), linksWithChat.get(0).getLinkId());
        assertEquals(1, linksWithChat.get(0).getChatIds().size());
    }

    @Test
    void findWithChatId_returnsOnlyLinksWithSpecifiedChatId() {
        LinkEntity link1 = createTestLink("https://github.com/repo1");
        LinkEntity link2 = createTestLink("https://github.com/repo2");
        getLinkRepository().saveAll(List.of(link1, link2));

        Long chatId1 = 11111L;
        Long chatId2 = 22222L;
        getLinkRepository().addChatId(link1, chatId1);
        getLinkRepository().addChatId(link2, chatId2);

        List<LinkEntity> results = getLinkRepository().findWithChatId(chatId1);

        assertEquals(1, results.size());
        assertEquals(link1.getLinkId(), results.get(0).getLinkId());
    }

    @Test
    void deleteByUrl_removesLinkAndAssociatedChatIds() {
        String url = "https://github.com/to/delete";
        LinkEntity link = createTestLink(url);
        getLinkRepository().save(link);
        getLinkRepository().addChatId(link, 99999L);

        Optional<LinkEntity> deleted = getLinkRepository().deleteByUrl(url);

        assertTrue(deleted.isPresent());
        assertEquals(url, deleted.orElseThrow().getUrl());
        assertTrue(getLinkRepository().findByUrl(url).isEmpty());

        List<LinkEntity> linksWithChat = getLinkRepository().findWithChatId(99999L);
        assertTrue(linksWithChat.isEmpty());
    }

    @Test
    void saveAll_persistsMultipleLinksWithDifferentTypes() {
        LinkEntity githubLink = createTestLink("https://github.com/repo1", LinkType.GITHUB);
        LinkEntity stackoverflowLink =
                createTestLink("https://stackoverflow.com/questions/123", LinkType.STACK_OVERFLOW);

        getLinkRepository().saveAll(List.of(githubLink, stackoverflowLink));

        List<LinkEntity> allLinks = getLinkRepository().findAll();
        assertEquals(2, allLinks.size());
        assertTrue(allLinks.stream().anyMatch(l -> l.getLinkType() == LinkType.GITHUB));
        assertTrue(allLinks.stream().anyMatch(l -> l.getLinkType() == LinkType.STACK_OVERFLOW));
    }

    @Test
    void findAllPaginated_returnsCorrectPagesInOrder() {
        List<LinkEntity> savedLinks = IntStream.rangeClosed(1, 5)
                .mapToObj(i -> createTestLink("https://github.com/repo" + i))
                .map(getLinkRepository()::save)
                .toList();

        List<Long> expectedIds =
                savedLinks.stream().map(LinkEntity::getLinkId).sorted().toList();

        List<LinkEntity> page1 = getLinkRepository().findAllPaginated(0, 2);
        assertThat(page1)
                .hasSize(2)
                .extracting(LinkEntity::getLinkId)
                .containsExactly(expectedIds.get(0), expectedIds.get(1));

        List<LinkEntity> page2 = getLinkRepository().findAllPaginated(expectedIds.get(1), 2);
        assertThat(page2)
                .hasSize(2)
                .extracting(LinkEntity::getLinkId)
                .containsExactly(expectedIds.get(2), expectedIds.get(3));

        List<LinkEntity> page3 = getLinkRepository().findAllPaginated(expectedIds.get(3), 2);
        assertThat(page3).hasSize(1).extracting(LinkEntity::getLinkId).containsExactly(expectedIds.get(4));

        List<LinkEntity> page4 = getLinkRepository().findAllPaginated(expectedIds.get(4), 2);
        assertThat(page4).isEmpty();
    }

    @Test
    void findAllPaginated_returnsEmptyWhenNoResults() {
        getLinkRepository().dropForTest();
        assertThat(getLinkRepository().findAllPaginated(0, 10)).isEmpty();
    }

    @Test
    void findAllPaginated_ordersResultsByLinkIdAsc() {
        getLinkRepository().save(createTestLink("https://github.com/repo3"));
        getLinkRepository().save(createTestLink("https://github.com/repo1"));
        getLinkRepository().save(createTestLink("https://github.com/repo2"));

        List<Long> resultIds = getLinkRepository().findAllPaginated(0, 3).stream()
                .map(LinkEntity::getLinkId)
                .toList();

        assertThat(resultIds).isSorted();
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
