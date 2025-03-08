package backend.academy.scrapper.clients;

import backend.academy.scrapper.repository.dto.StackAnswersResponseDto;
import java.time.Instant;
import backend.academy.scrapper.repository.dto.StackResponseDto;
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

    public Mono<StackResponseDto> getStackOverflowNewAnswers(Long questionId, Instant fromDate) {
        return webClient.get()
            .uri("/questions/{questionId}/answers", questionId)
            .attribute("sort", "activity")
            .attribute("fromdate", fromDate.toEpochMilli())
            .attribute("order", "desc")
            .retrieve()
            .bodyToMono(StackResponseDto.class);
    }
}
