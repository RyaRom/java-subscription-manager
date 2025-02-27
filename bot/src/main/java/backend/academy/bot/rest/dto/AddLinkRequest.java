package backend.academy.bot.rest.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record AddLinkRequest(
    String link,
    List<String> tags,
    List<String> filters
) {
}
