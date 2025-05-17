package backend.academy.scrapper.repository.links.dto.stackOverflow;

import java.util.List;

public record StackResponseForQuestionInfoDto(List<StackQuestionResponse> items) {
    public record StackQuestionResponse(String title) {}
}
