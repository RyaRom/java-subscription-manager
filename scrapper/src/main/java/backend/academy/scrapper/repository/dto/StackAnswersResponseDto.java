package backend.academy.scrapper.repository.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StackAnswersResponseDto(@JsonProperty("answer_id") Long answerId) {

    public String getLink() {
        return String.format("https://stackoverflow.com/a/%d", answerId);
    }
}
