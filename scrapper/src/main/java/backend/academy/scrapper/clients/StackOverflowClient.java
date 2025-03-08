package backend.academy.scrapper.clients;

import backend.academy.scrapper.config.ScrapperConfig.StackOverflowCredentials;
import backend.academy.scrapper.repository.dto.StackResponseDto;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class StackOverflowClient {
    @Qualifier("stackOverflowHttpClient")
    private final WebClient webClient;

    private final StackOverflowCredentials credentials;

    public Mono<StackResponseDto> getStackOverflowNewAnswers(Long questionId, Instant fromDate) {
        var builder = webClient.get();

        if (credentials.tokenDisabled()) {
            return builder.uri(uriBuilder -> {
                        var uri = uriBuilder
                                .path("/questions/{questionId}/answers")
                                .queryParam("sort", "activity")
                                .queryParam("site", "stackoverflow")
                                .queryParam("fromdate", fromDate.toEpochMilli() / 1000)
                                .queryParam("order", "desc")
                                .build(questionId);
                        log.info("uri {}", uri);
                        return uri;
                    })
                    .retrieve()
                    .bodyToMono(StackResponseDto.class);
        } else {
            return builder.uri(uriBuilder -> uriBuilder
                            .path("/questions/{questionId}/answers")
                            .queryParam("sort", "activity")
                            .queryParam("fromdate", fromDate.toEpochMilli() / 1000)
                            .queryParam("order", "desc")
                            .queryParam("site", "stackoverflow")
                            .queryParam("key", credentials.key())
                            .queryParam("access_token", credentials.accessToken())
                            .build(questionId))
                    .retrieve()
                    .bodyToMono(StackResponseDto.class);
        }
    }
}
