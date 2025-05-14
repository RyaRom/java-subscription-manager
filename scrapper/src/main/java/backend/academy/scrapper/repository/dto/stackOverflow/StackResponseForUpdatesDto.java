package backend.academy.scrapper.repository.dto.stackOverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

public record StackResponseForUpdatesDto(
    List<StackAnswersResponseDto> items
) {
    public record StackAnswersResponseDto(
        @JsonProperty("creation_date") Instant creationDate,
        @JsonProperty("body_markdown") String bodyMarkdown,
        @JsonProperty("owner") Owner owner
    ) {
    }

    public record Owner(
        @JsonProperty("display_name")
        String displayName
    ) {
    }
}
