package backend.academy.bot.clients.kafka;

import backend.academy.bot.telegram.sdk.utils.TelegramAPI;
import backend.academy.dto.LinkUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Log4j2
@RequiredArgsConstructor
public class LinkUpdatesConsumer {
    private final TelegramAPI telegramAPI;

    @KafkaListener(topics = "${spring.kafka.kafka-topics-names.link-updates}")
    public void getUpdate(@Payload LinkUpdate linkUpdate,
                          ConsumerRecord<String, String> record) {
        log.info("Received update: {}", linkUpdate);

        telegramAPI.sendMessagesAsync(linkUpdate)
            .subscribe();
    }
}
