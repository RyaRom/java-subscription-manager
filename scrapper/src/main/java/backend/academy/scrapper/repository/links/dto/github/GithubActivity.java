package backend.academy.scrapper.repository.links.dto.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.time.OffsetDateTime;
import lombok.extern.log4j.Log4j2;

@Log4j2
public record GithubActivity(
        Long id,
        @JsonProperty("node_id") String nodeId,
        String before,
        String after,
        String ref,
        OffsetDateTime timestamp,
        @JsonProperty("activity_type") ActivityType activityType,
        Actor actor) {
    public enum ActivityType {
        PUSH("push"),
        FORCE_PUSH("force_push"),
        BRANCH_DELETION("branch_deletion"),
        BRANCH_CREATION("branch_creation"),
        PR_MERGE("pr_merge"),
        MERGE_QUEUE_MERGE("merge_queue_merge"),
        UNKNOWN("unknown");

        private final String value;

        ActivityType(String value) {
            this.value = value;
        }

        @JsonCreator
        public static ActivityType fromValue(String value) {
            for (ActivityType type : values()) {
                if (type.value.equalsIgnoreCase(value)) {
                    return type;
                }
            }
            log.warn("Unknown activity type: {}", value);
            return UNKNOWN;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public record Actor(
            String name,
            String email,
            String login,
            Long id,
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
