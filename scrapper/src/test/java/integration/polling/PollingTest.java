package integration.polling;

import backend.academy.scrapper.clients.BotClient;
import backend.academy.scrapper.clients.GithubClient;
import backend.academy.scrapper.clients.StackOverflowClient;
import backend.academy.scrapper.repository.links.LinkRepository;
import backend.academy.scrapper.repository.links.dto.LinkType;
import backend.academy.scrapper.repository.links.dto.github.GithubFullInfo;
import backend.academy.scrapper.repository.links.dto.stackOverflow.StackOverflowFullInfo;
import backend.academy.scrapper.repository.links.dto.stackOverflow.StackResponseForUpdatesDto;
import backend.academy.scrapper.repository.links.entities.ChatIdEntity;
import backend.academy.scrapper.repository.links.entities.GithubInfoEntity;
import backend.academy.scrapper.repository.links.entities.LinkEntity;
import backend.academy.scrapper.repository.links.entities.StackOverflowInfoEntity;
import backend.academy.scrapper.service.UpdatePollingJob;
import integration.BaseIntegrationTest;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PollingTest extends BaseIntegrationTest {
    public static final GithubInfoEntity GITHUB_INFO_ENTITY = new GithubInfoEntity("academy-frontend",
        "academy-frontend");
    public static final StackOverflowInfoEntity STACK_OVERFLOW_INFO_ENTITY = new StackOverflowInfoEntity(1732348L);
    private final LinkEntity githubLink = new LinkEntity()
        .setUrl("https://github.com/academy-frontend/academy-frontend")
        .setLinkType(LinkType.GITHUB)
        .setLinkInfo(GITHUB_INFO_ENTITY)
        .setChatIds(List.of(new ChatIdEntity().setChatId(1L), new ChatIdEntity().setChatId(2L)));
    private final LinkEntity soLink = new LinkEntity()
        .setUrl("https://stackoverflow.com/questions/1732348/text")
        .setLinkInfo(STACK_OVERFLOW_INFO_ENTITY)
        .setChatIds(List.of(new ChatIdEntity().setChatId(1L), new ChatIdEntity().setChatId(2L)))
        .setLinkType(LinkType.STACK_OVERFLOW);

    @Autowired
    private UpdatePollingJob updatePollingJob;
    @Autowired
    private LinkRepository linkRepository;
    @MockitoBean
    private GithubClient githubClient;
    @MockitoBean
    private StackOverflowClient stackOverflowClient;
    @MockitoBean
    private BotClient botClient;

    @BeforeEach
    void setUp() {
        when(githubClient.getRepoActivities(any(), any())).thenReturn(Flux.empty());
        when(stackOverflowClient.getStackOverflowNewAnswers(any(), any())).thenReturn(Mono.empty());
        when(botClient.sendUpdate(any(GithubFullInfo.class), any())).thenReturn(Mono.empty());
        when(botClient.sendUpdate(any(StackOverflowFullInfo.class), any())).thenReturn(Mono.empty());

        GITHUB_INFO_ENTITY.setLink(githubLink);
        STACK_OVERFLOW_INFO_ENTITY.setLink(soLink);
    }

    @AfterEach
    void tearDown() {
        linkRepository.dropForTest();
    }

    @Test
    void updateTest() {
        linkRepository.saveAll(List.of(githubLink, soLink));
        updatePollingJob.update();

        verify(githubClient, times(1))
            .getRepoActivities("academy-frontend", "academy-frontend");
        verify(stackOverflowClient, times(1))
            .getStackOverflowNewAnswers(eq(1732348L), any());
    }

    @Test
    void botUpdateTest() {
        var response = new StackResponseForUpdatesDto(List.of(new StackResponseForUpdatesDto.StackAnswersResponseDto(
            Instant.ofEpochMilli(123),
            "body",
            new StackResponseForUpdatesDto.Owner("Name"))));
        when(stackOverflowClient.getStackOverflowNewAnswers(eq(1732348L), any()))
            .thenReturn(Mono.just(response));
        linkRepository.save(soLink);
        updatePollingJob.update();

        verify(botClient, times(1))
            .sendUpdate(any(StackOverflowFullInfo.class), eq(soLink));
    }
}
