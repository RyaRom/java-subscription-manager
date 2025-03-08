package integration.polling;

import backend.academy.scrapper.clients.BotClient;
import backend.academy.scrapper.clients.GithubClient;
import backend.academy.scrapper.clients.StackOverflowClient;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.dto.GithubResponseDto.GithubActivity;
import backend.academy.scrapper.repository.dto.Link;
import backend.academy.scrapper.repository.dto.StackAnswersResponseDto;
import backend.academy.scrapper.repository.dto.StackResponseDto;
import backend.academy.scrapper.service.UpdatePollingJob;
import integration.BaseIntegrationTest;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PollingTest extends BaseIntegrationTest {
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

    private Link githubLink = Link.builder()
        .url("https://github.com/academy-frontend/academy-frontend")
        .linkType(Link.Type.GITHUB)
        .chatIds(List.of(1L, 2L))
        .build();

    private Link soLink = Link.builder()
        .url("https://stackoverflow.com/questions/1732348/text")
        .chatIds(List.of(1L, 2L))
        .linkType(Link.Type.STACK_OVERFLOW)
        .build();

    @BeforeEach
    void setUp() {
        when(githubClient.getRepoActivities(any(), any())).thenReturn(Mono.empty());
        when(stackOverflowClient.getStackOverflowNewAnswers(any(), any())).thenReturn(Mono.empty());
        when(botClient.sendUpdate(any(GithubActivity.class), any())).thenReturn(Mono.empty());
        when(botClient.sendUpdate(any(StackAnswersResponseDto.class), any())).thenReturn(Mono.empty());
    }

    @AfterEach
    void tearDown() {
        linkRepository.drop();
    }

    @Test
    void updateTest() {
        linkRepository.save(List.of(githubLink, soLink));
        updatePollingJob.update();

        verify(githubClient, times(1))
            .getRepoActivities("academy-frontend", "academy-frontend");
        verify(stackOverflowClient, times(1))
            .getStackOverflowNewAnswers(eq(1732348L), any());
    }

    @Test
    void botUpdateTest() {
        var response = new StackResponseDto(List.of(
            new StackAnswersResponseDto(1L)
        ), false);
        when(stackOverflowClient.getStackOverflowNewAnswers(eq(1732348L), any())).thenReturn(Mono.just(response));
        linkRepository.save(soLink);
        updatePollingJob.update();

        verify(botClient, times(1))
            .sendUpdate(response.items().getFirst(), soLink);
    }
}
