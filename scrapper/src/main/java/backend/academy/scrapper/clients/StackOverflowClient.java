package backend.academy.scrapper.clients;

import backend.academy.scrapper.config.ScrapperConfig.StackOverflowCredentials;
import backend.academy.scrapper.repository.dto.StackResponseDto;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class StackOverflowClient {
    private final WebClient stackOverflowHttpClient;

    private final StackOverflowCredentials credentials;

    public Mono<StackResponseDto> getStackOverflowNewAnswers(Long questionId, Instant fromDate) {
        var builder = stackOverflowHttpClient.get();

        if (credentials.tokenDisabled()) {
            return builder.uri(uriBuilder -> {
                        UriBuilder building = uriBuilder.path("/questions/{questionId}/answers");
                        basicQueries(building, fromDate);
                        return building.build(questionId);
                    })
                    .retrieve()
                    .bodyToMono(StackResponseDto.class);
        } else {
            return builder.uri(uriBuilder -> {
                        UriBuilder building = uriBuilder.path("/questions/{questionId}/answers");
                        basicQueries(building, fromDate);
                        return building.queryParam("key", credentials.key())
                                .queryParam("access_token", credentials.accessToken())
                                .build(questionId);
                    })
                    .retrieve()
                    .bodyToMono(StackResponseDto.class);
        }
    }

    private void basicQueries(UriBuilder builder, Instant fromDate) {
        builder.queryParam("sort", "activity")
                .queryParam("site", "stackoverflow")
                .queryParam("fromdate", fromDate.toEpochMilli() / 1000)
                .queryParam("order", "desc");
    }
}
