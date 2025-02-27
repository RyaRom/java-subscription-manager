package backend.academy.bot.rest.dto;

import java.util.List;

public record ListLinkResponse(
    List<LinkResponse> links,
    Integer size
) {
}
