package backend.academy.bot.clients;

import backend.academy.bot.clients.kafka.DLQPublisher;
import backend.academy.bot.telegram.sdk.utils.TelegramAPI;
import backend.academy.dto.LinkUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Log4j2
@RequiredArgsConstructor
public class LinkUpdatesConsumer {
    private final TelegramAPI telegramAPI;
    private final DLQPublisher dlqPublisher;

    @KafkaListener(
        topics = "${spring.kafka.kafka-topics-names.link-updates}"
    )
    public void getUpdate(@Payload LinkUpdate linkUpdate) {
        log.info("received update {}", linkUpdate);
        telegramAPI.sendMessagesAsync(linkUpdate)
            .onErrorMap(e -> {
                dlqPublisher.sendError(e);
                return e;
            })
            .subscribe();
    }
}
