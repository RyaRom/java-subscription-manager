package backend.academy.scrapper.repository.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record StackResponseDto(
    List<StackAnswersResponseDto> items,
    @JsonProperty("has_more")
    boolean hasMore
) {
}
