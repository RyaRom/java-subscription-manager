package backend.academy.scrapper.clients;

import backend.academy.scrapper.repository.dto.GithubActivity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class GithubClient {
    @Qualifier("githubHttpClient")
    private final WebClient webClient;

    public Flux<GithubActivity> getRepoActivities(String owner, String repo) {
        return webClient
            .get()
            .uri("/repos/{owner}/{repo}/activity", owner, repo)
            .retrieve()
            .bodyToFlux(GithubActivity.class);
    }
}
