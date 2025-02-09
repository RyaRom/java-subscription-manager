package backend.academy.scrapper.dto;

import java.util.List;

public record LinkResponse(
    Long chatId,
    String url,
    List<String> tags,
    List<String> filters
) {
}
