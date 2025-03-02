package backend.academy.bot.rest.dto;

import java.util.List;

public record LinkUpdate(
    Long linkId,
    String url,
    String description,
    List<Long> tgChatIds
) {
}
