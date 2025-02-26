package backend.academy.bot.rest.dto;

import java.util.List;

public record AddLinkRequest(
    String link,
    List<String> tags,
    List<String> filters
) {
}
