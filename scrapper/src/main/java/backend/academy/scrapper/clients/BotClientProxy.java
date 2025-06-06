package backend.academy.scrapper.clients;

import backend.academy.dto.LinkUpdate;
import backend.academy.resilience2.CircuitBreaker;
import backend.academy.resilience2.Fallback;
import backend.academy.resilience2.Retry;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Log4j2
public class BotClientProxy implements BotClient {
    private final BotKafkaClient botKafkaClient;
    private final BotHttpClient botHttpClient;
    private BotClient delegate;

    @Retry
    @CircuitBreaker
    @Fallback("switchStrategy")
    @Override
    public Mono<Void> sendUpdate(LinkUpdate linkUpdate) {
        return delegate.sendUpdate(linkUpdate);
    }

    public Mono<Void> switchStrategy(LinkUpdate linkUpdate) {
        return Mono.fromRunnable(() -> {
            log.info("Switching strategy to {}",
                delegate instanceof BotKafkaClient ? "http" : "kafka");
            if (delegate instanceof BotKafkaClient) {
                delegate = botHttpClient;
            } else {
                delegate = botKafkaClient;
            }
        });
    }
}
