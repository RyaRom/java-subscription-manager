package integration.polling;

import backend.academy.scrapper.clients.BotClient;
import backend.academy.scrapper.clients.GithubHttpClient;
import backend.academy.scrapper.clients.StackOverflowHttpClient;
import backend.academy.scrapper.repository.links.LinkRepository;
import backend.academy.scrapper.repository.links.dto.LinkType;
import backend.academy.scrapper.repository.links.dto.github.GithubActivityResponse;
import backend.academy.scrapper.repository.links.dto.github.GithubFullInfo;
import backend.academy.scrapper.repository.links.dto.github.GithubIssueOrPrResponse;
import backend.academy.scrapper.repository.links.dto.stackOverflow.StackOverflowFullInfo;
import backend.academy.scrapper.repository.links.dto.stackOverflow.StackResponseForUpdatesDto;
import backend.academy.scrapper.repository.links.entities.ChatIdEntity;
import backend.academy.scrapper.repository.links.entities.GithubInfoEntity;
import backend.academy.scrapper.repository.links.entities.LinkEntity;
import backend.academy.scrapper.repository.links.entities.StackOverflowInfoEntity;
import backend.academy.scrapper.service.UpdatePollingJob;
import integration.BaseIntegrationTest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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
    public static final GithubInfoEntity GITHUB_INFO_ENTITY =
        new GithubInfoEntity("academy-frontend", "academy-frontend");
    public static final StackOverflowInfoEntity STACK_OVERFLOW_INFO_ENTITY = new StackOverflowInfoEntity(1732348L);
    private final LinkEntity githubLink = new LinkEntity()
        .setUrl("https://github.com/academy-frontend/academy-frontend")
        .setLinkType(LinkType.GITHUB)
        .setLinkInfo(GITHUB_INFO_ENTITY);
    private final LinkEntity soLink = new LinkEntity()
        .setUrl("https://stackoverflow.com/questions/1732348/text")
        .setLinkInfo(STACK_OVERFLOW_INFO_ENTITY)
        .setLinkType(LinkType.STACK_OVERFLOW);

    @Autowired
    private UpdatePollingJob updatePollingJob;

    @Autowired
    private LinkRepository linkRepository;

    @MockitoBean
    private GithubHttpClient githubHttpClient;

    @MockitoBean
    private StackOverflowHttpClient stackOverflowHttpClient;

    @MockitoBean
    private BotClient botClient;

    @BeforeEach
    void setUp() {
        when(githubHttpClient.getRepoActivities(any(), any())).thenReturn(Flux.empty());
        when(githubHttpClient.getRepoPulls(any(), any())).thenReturn(Flux.empty());
        when(githubHttpClient.getRepoIssues(any(), any())).thenReturn(Flux.empty());
        when(stackOverflowHttpClient.getStackOverflowNewAnswers(any(), any())).thenReturn(Mono.empty());
        when(stackOverflowHttpClient.getStackOverflowNewComments(any(), any())).thenReturn(Mono.empty());
        when(stackOverflowHttpClient.getQuestionTitle(any())).thenReturn(Mono.empty());
        when(botClient.sendUpdate(any())).thenReturn(Mono.empty());

        GITHUB_INFO_ENTITY.setLink(githubLink);
        STACK_OVERFLOW_INFO_ENTITY.setLink(soLink);
        githubLink.setChatIds(List.of(new ChatIdEntity(1L, githubLink), new ChatIdEntity(2L, githubLink)));
        soLink.setChatIds(List.of(new ChatIdEntity(1L, soLink), new ChatIdEntity(2L, soLink)));
    }

    @AfterEach
    void tearDown() {
        linkRepository.dropForTest();
    }

    @Test
    void updatePageTest() throws InterruptedException {
        linkRepository.saveAll(List.of(githubLink, soLink));
        updatePollingJob.update();
        Thread.sleep(1000L);

        verify(githubHttpClient, times(1)).getRepoActivities("academy-frontend", "academy-frontend");
        verify(githubHttpClient, times(1)).getRepoPulls("academy-frontend", "academy-frontend");
        verify(githubHttpClient, times(1)).getRepoIssues("academy-frontend", "academy-frontend");
        verify(stackOverflowHttpClient, times(1)).getStackOverflowNewAnswers(eq(1732348L), any());
        verify(stackOverflowHttpClient, times(1)).getStackOverflowNewComments(eq(1732348L), any());
    }

    @Test
    void botUpdateTest() {
        StackResponseForUpdatesDto stackResponseForUpdatesDto =
            new StackResponseForUpdatesDto(List.of(new StackResponseForUpdatesDto.StackAnswersResponseDto(
                Instant.ofEpochMilli(123), "body", new StackResponseForUpdatesDto.Owner("Name"))));
        GithubIssueOrPrResponse githubIssueOrPrResponse = new GithubIssueOrPrResponse(
            "title",
            "body",
            OffsetDateTime.of(2100, 1, 26, 19, 6, 43, 0, ZoneOffset.UTC).toString(),
            new GithubIssueOrPrResponse.User("user"));
        GithubActivityResponse githubActivityResponse = new GithubActivityResponse(
            OffsetDateTime.of(2100, 1, 26, 19, 6, 43, 0, ZoneOffset.UTC),
            GithubActivityResponse.ActivityType.PR_MERGE,
            new GithubActivityResponse.Actor("user"));
        StackOverflowFullInfo stackOverflowFullInfoAnswer = new StackOverflowFullInfo(
            "Question title",
            "Name",
            LocalDateTime.from(Instant.ofEpochMilli(123).atZone(ZoneId.of("UTC")))
                .format(DateTimeFormatter.BASIC_ISO_DATE),
            "body",
            "Answer");
        StackOverflowFullInfo stackOverflowFullInfoComments = new StackOverflowFullInfo(
            "Question title",
            "Name",
            LocalDateTime.from(Instant.ofEpochMilli(123).atZone(ZoneId.of("UTC")))
                .format(DateTimeFormatter.BASIC_ISO_DATE),
            "body",
            "Comment");
        GithubFullInfo githubFullInfoIssue =
            new GithubFullInfo("title", "user", "2100-01-26T19:06:43Z", "body", "issue");
        GithubFullInfo githubFullInfoPr = new GithubFullInfo("title", "user", "2100-01-26T19:06:43Z", "body", "pr");

        GithubFullInfo githubFullInfoActivity =
            new GithubFullInfo("Activity", "user", "2100-01-26T19:06:43Z", "git update", "pr_merge");

        when(stackOverflowHttpClient.getStackOverflowNewAnswers(eq(1732348L), any()))
            .thenReturn(Mono.just(stackResponseForUpdatesDto));
        when(stackOverflowHttpClient.getStackOverflowNewComments(eq(1732348L), any()))
            .thenReturn(Mono.just(stackResponseForUpdatesDto));
        when(stackOverflowHttpClient.getQuestionTitle(eq(1732348L))).thenReturn(Mono.just("Question title"));
        when(githubHttpClient.getRepoPulls(any(), any()))
            .thenReturn(Flux.fromIterable(List.of(githubIssueOrPrResponse)));
        when(githubHttpClient.getRepoIssues(any(), any()))
            .thenReturn(Flux.fromIterable(List.of(githubIssueOrPrResponse)));
        when(githubHttpClient.getRepoActivities(any(), any()))
            .thenReturn(Flux.fromIterable(List.of(githubActivityResponse)));
        linkRepository.saveAll(List.of(soLink, githubLink));
        updatePollingJob.update();

        verify(botClient, times(5)).sendUpdate(any());
    }
}
