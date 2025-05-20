package backend.academy.kafka;

public record RemoveLinkKafkaRequest(
    Long chatId,
    String link
) {
}
