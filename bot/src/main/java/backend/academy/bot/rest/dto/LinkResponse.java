package backend.academy.bot.rest.dto;

import java.util.List;

public record LinkResponse(
    Long chatId,
    String url,
    List<String> tags,
    List<String> filters
) {
}
