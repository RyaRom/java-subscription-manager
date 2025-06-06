package backend.academy.scrapper.clients;

import backend.academy.dto.LinkUpdate;
import backend.academy.resilience2.Fallback;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Log4j2
public class BotKafkaClient implements BotClient {
    private final KafkaTemplate<Object, Object> kafkaTemplate;

    @Value("${spring.kafka.kafka-topics.link-updates.name}")
    public String linkUpdatesTopic;

    @Override
    @Fallback("switchToHttp")
    public Mono<Void> sendUpdate(LinkUpdate linkUpdate) {
        return Mono.fromFuture(kafkaTemplate.send(linkUpdatesTopic, linkUpdate))
                .doOnEach(signal -> {
                    if (signal.isOnError()) {
                        log.warn("Failing to send {}: {}", linkUpdate, signal.getThrowable());
                    } else {
                        log.info("Successfully sent {}", linkUpdate);
                    }
                })
                .then();
    }
}
