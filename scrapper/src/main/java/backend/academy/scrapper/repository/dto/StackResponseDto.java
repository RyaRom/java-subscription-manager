package backend.academy.scrapper.repository.dto;

import java.util.List;

public record StackResponseDto(
    List<StackAnswersResponseDto> items,
    boolean hasMore
) {
}
