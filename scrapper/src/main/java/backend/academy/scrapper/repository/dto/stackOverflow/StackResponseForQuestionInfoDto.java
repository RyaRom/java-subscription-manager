package backend.academy.scrapper.repository.dto.stackOverflow;

import java.util.List;

public record StackResponseForQuestionInfoDto(
    List<StackQuestionResponse> items
) {
    public record StackQuestionResponse(
        String title
    ) {
    }
}
