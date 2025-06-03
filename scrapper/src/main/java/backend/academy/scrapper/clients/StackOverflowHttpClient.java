package backend.academy.scrapper.clients;

import backend.academy.resilience2.Retry;
import backend.academy.scrapper.config.ScrapperConfig.StackOverflowCredentials;
import backend.academy.scrapper.repository.links.dto.stackOverflow.StackResponseForQuestionInfoDto;
import backend.academy.scrapper.repository.links.dto.stackOverflow.StackResponseForUpdatesDto;
import backend.academy.scrapper.resilience.RetryService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

@Log4j2
@Component
@RequiredArgsConstructor
public class StackOverflowHttpClient {
    private final WebClient stackOverflowWebClient;
    private final RetryService retryService;
    private final StackOverflowCredentials credentials;

    @Retry
    public Mono<StackResponseForUpdatesDto> getStackOverflowNewAnswers(Long questionId, Instant fromDate) {
        var builder = stackOverflowWebClient.get();
        return retryService.withRetry(builder.uri(uriBuilder -> {
                    UriBuilder building = uriBuilder.path("/questions/{questionId}/answers");
                    basicQueriesFromDate(building, fromDate, true);
                    addCredentials(building);
                    return building.build(questionId);
                })
                .retrieve()
                .bodyToMono(StackResponseForUpdatesDto.class));
    }

    @Retry
    public Mono<StackResponseForUpdatesDto> getStackOverflowNewComments(Long questionId, Instant fromDate) {
        var builder = stackOverflowWebClient.get();
        return retryService.withRetry(builder.uri(uriBuilder -> {
                    UriBuilder building = uriBuilder.path("/questions/{questionId}/comments");
                    basicQueriesFromDate(building, fromDate, false);
                    addCredentials(building);
                    return building.build(questionId);
                })
                .retrieve()
                .bodyToMono(StackResponseForUpdatesDto.class));
    }

    @Retry
    public Mono<String> getQuestionTitle(Long questionId) {
        var builder = stackOverflowWebClient.get();
        var request = builder.uri(uriBuilder -> {
                    UriBuilder building = uriBuilder.path("/questions/{questionId}");
                    basicQueries(building);
                    addCredentials(building);
                    return building.build(questionId);
                })
                .retrieve()
                .bodyToMono(StackResponseForQuestionInfoDto.class);
        return retryService.withRetry(request).map(it -> {
            var items = it.items();
            if (items.isEmpty()) {
                log.error("QuestionId {} doesn't have real question", questionId);
                throw new IllegalStateException("Question is broken: " + questionId);
            }
            return items.getFirst().title();
        });
    }

    private void addCredentials(UriBuilder builder) {
        if (!credentials.tokenDisabled()) {
            builder.queryParam("key", credentials.key()).queryParam("access_token", credentials.accessToken());
        }
    }

    private void basicQueriesFromDate(UriBuilder builder, Instant fromDate, boolean withSort) {
        builder.queryParam("site", "stackoverflow")
                .queryParam("fromdate", fromDate.toEpochMilli() / 1000)
                .queryParam("order", "desc")
                .queryParam("filter", "!6WPIompfyuc1r");
        if (withSort) {
            builder.queryParam("sort", "activity");
        }
    }

    private void basicQueries(UriBuilder builder) {
        builder.queryParam("sort", "activity")
                .queryParam("site", "stackoverflow")
                .queryParam("order", "desc")
                .queryParam("filter", "!6WPIompfyuc1r");
    }
}
