package backend.academy.bot.clients.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class DLQPublisher {
    private final KafkaTemplate<Object, Object> kafkaTemplate;

    @Value("${spring.kafka.kafka-topics-names.dlq}")
    private String dlq;

    public void sendError(Throwable e) {
        kafkaTemplate.send(
                dlq,
                new ExceptionMessage(e.getMessage(), e.getClass(), e.getCause().getClass()));
    }

    private record ExceptionMessage(
            String message, Class<? extends Throwable> type, Class<? extends Throwable> cause) {}
}
