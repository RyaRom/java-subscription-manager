package backend.academy.scrapper.repository.dto;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

public record GithubResponseDto(List<GithubActivity> activities) {

    public enum ActivityType {
        PUSH("push"),
        FORCE_PUSH("force_push"),
        BRANCH_DELETION("branch_deletion"),
        BRANCH_CREATION("branch_creation"),
        PR_MERGE("pr_merge"),
        MERGE_QUEUE_MERGE("merge_queue_merge");

        private final String value;

        ActivityType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public record GithubActivity(
            int id,
            String nodeId,
            String before,
            String after,
            String ref,
            OffsetDateTime timestamp,
            ActivityType activityType,
            Actor actor) {}

    public record Actor(
            String name,
            String email,
            String login,
            long id,
            String nodeId,
            URI avatarUrl,
            String gravatarId,
            URI url,
            URI htmlUrl,
            URI followersUrl,
            URI followingUrl,
            URI gistsUrl,
            URI starredUrl,
            URI subscriptionsUrl,
            URI organizationsUrl,
            URI reposUrl,
            URI eventsUrl,
            URI receivedEventsUrl,
            String type,
            boolean siteAdmin,
            String starredAt,
            String userViewType) {}
}
