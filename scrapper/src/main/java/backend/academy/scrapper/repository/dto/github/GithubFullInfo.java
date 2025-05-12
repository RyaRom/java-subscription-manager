package backend.academy.scrapper.repository.dto.github;

public record GithubFullInfo(
    String title,
    String username,
    String creationTime,
    String body,
    String type
) {
    public static GithubFullInfo fromResponse(
        GithubActivity activity
    ) {
        return new GithubFullInfo(
            "", "", "", "", ""
        );
    }
}
