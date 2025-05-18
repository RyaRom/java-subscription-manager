package backend.academy.bot.clients;

import backend.academy.bot.telegram.sdk.utils.TelegramAPI;
import backend.academy.dto.LinkUpdate;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Log4j2
@RequiredArgsConstructor
public class LinkUpdatesConsumer {
    private final TelegramAPI telegramAPI;

    @KafkaListener(
        topics = "${spring.kafka.kafka-topics-names.link-updates}",
        errorHandler = "kafkaErrorHandler"
    )
    public void getUpdate(@Payload LinkUpdate linkUpdate) {
        log.info("received update {}", linkUpdate);
        telegramAPI.sendMessagesAsync(linkUpdate)
            .subscribe();
    }
}
