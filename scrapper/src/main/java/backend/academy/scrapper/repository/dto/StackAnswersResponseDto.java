package backend.academy.scrapper.repository.dto;

public record StackAnswersResponseDto(
    Long answerId
) {

    public String getLink() {
        return String.format("https://stackoverflow.com/a/%d", answerId);
    }
}
