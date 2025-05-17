package backend.academy.scrapper.repository.links.dto.github;

public record GithubFullInfo(
    String title,
    String username,
    String creationTime,
    String body,
    String type
) {
    public static GithubFullInfo fromResponse(
        GithubActivityResponse activity
    ) {
        return new GithubFullInfo(
            "Activity",
            activity.actor().login(),
            activity.timestamp().toString(),
            "git update",
            activity.activityType().toString()
        );
    }

    public static GithubFullInfo fromUpdate(
        GithubIssueOrPrResponse githubIssueOrPrResponse,
        String type
    ) {
        return new GithubFullInfo(
            githubIssueOrPrResponse.title(),
            githubIssueOrPrResponse.user().login(),
            githubIssueOrPrResponse.updatedAt(),
            githubIssueOrPrResponse.body(),
            type
        );
    }
}
