package backend.academy.scrapper.repository.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record StackAnswersResponseDto(
    @JsonProperty("answer_id") Long answerId,
    @JsonProperty("creation_date") Instant creationDate,
    @JsonProperty("owner") Owner owner,
    @JsonProperty("body_markdown") String bodyMarkdown
) {

    public String getLink() {
        return String.format("https://stackoverflow.com/a/%d", answerId);
    }

    public record Owner(
        @JsonProperty("display_name") String displayName
    ) {
    }
}
