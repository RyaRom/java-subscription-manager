package backend.academy.bot.controller;

import java.util.List;

public record LinkUpdate(
    Long chatId,
    String url,
    String description,
    List<Long> tgChatIds
) {
}
