package backend.academy.scrapper.repository.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubIssueOrPrResponse(
    String title,
    String body,
    @JsonProperty("updated_at")
    String updatedAt,
    User user
) {
    public record User(
        String login
    ) {
    }
}
