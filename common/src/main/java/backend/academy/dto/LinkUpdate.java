package backend.academy.dto;

import java.util.List;

public record LinkUpdate(
    Long linkId,
    String url,
    String description,
    List<Long> tgChatIds
) {
}
