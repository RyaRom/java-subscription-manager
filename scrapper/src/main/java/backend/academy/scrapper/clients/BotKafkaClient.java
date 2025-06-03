package backend.academy.scrapper.clients;

import backend.academy.dto.LinkUpdate;
import backend.academy.resilience2.Fallback;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Log4j2
public class BotKafkaClient implements BotClient {
    @Value("${spring.kafka.kafka-topics.link-updates.name}")
    public String linkUpdatesTopic;

    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final ApplicationContext applicationContext;

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

    public Mono<Void> switchToHttp(LinkUpdate linkUpdate) {
        var proxy = applicationContext.getBean(BotClientProxy.class);
        var httpClient = applicationContext.getBean(BotHttpClient.class);
        proxy.switchStrategy(httpClient);
        return httpClient.sendUpdate(linkUpdate);
    }
}
