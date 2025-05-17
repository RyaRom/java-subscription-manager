package backend.academy.scrapper.clients;

import backend.academy.scrapper.repository.links.dto.github.GithubActivityResponse;
import backend.academy.scrapper.repository.links.dto.github.GithubIssueOrPrResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class GithubHttpClient {
    private final WebClient githubWebClient;

    public Flux<GithubActivityResponse> getRepoActivities(String owner, String repo) {
        return githubWebClient
                .get()
                .uri("/repos/{owner}/{repo}/activity", owner, repo)
                .retrieve()
                .bodyToFlux(GithubActivityResponse.class);
    }

    public Flux<GithubIssueOrPrResponse> getRepoIssues(String owner, String repo) {
        return githubWebClient
                .get()
                .uri("/repos/{owner}/{repo}/issues", owner, repo)
                .retrieve()
                .bodyToFlux(GithubIssueOrPrResponse.class);
    }

    public Flux<GithubIssueOrPrResponse> getRepoPulls(String owner, String repo) {
        return githubWebClient
                .get()
                .uri("/repos/{owner}/{repo}/pulls", owner, repo)
                .retrieve()
                .bodyToFlux(GithubIssueOrPrResponse.class);
    }
}
