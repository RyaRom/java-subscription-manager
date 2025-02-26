package backend.academy.bot.rest.dto;

import java.util.List;

public record ListLinkResponse(
    List<String> links,
    Integer size
) {
}
