package backend.academy.bot.rest.dto;

import java.util.List;

public record LinkUpdate(
    Long chatId,
    String url,
    String description,
    List<Long> tgChatIds
) {
}
