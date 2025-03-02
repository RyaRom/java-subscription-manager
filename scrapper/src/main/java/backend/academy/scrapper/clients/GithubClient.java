package backend.academy.scrapper.clients;

import static java.lang.String.format;

import backend.academy.scrapper.repository.dto.GithubResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class GithubClient {
    @Qualifier("githubHttpClient")
    private final WebClient webClient;

    public Mono<GithubResponseDto> getRepoActivities(String owner, String repo) {
        return webClient
                .get()
                .uri(format("/repos/%s/%s/activity", owner, repo))
                .retrieve()
                .bodyToMono(GithubResponseDto.class);
    }
}
