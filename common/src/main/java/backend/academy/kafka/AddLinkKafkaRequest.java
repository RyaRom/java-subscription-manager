package backend.academy.kafka;

import java.util.List;

public record AddLinkKafkaRequest(
    Long chatId,
    String link,
    List<String> tags,
    List<String> filters
) {
}
