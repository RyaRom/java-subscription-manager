package backend.academy.scrapper.repository;

import java.util.List;

public record Subscription(
    Long chatId,
    String url,
    List<String> filters,
    List<String> tags
) {
}
