package backend.academy.scrapper.clients;

import backend.academy.dto.LinkUpdate;
import backend.academy.resilience2.CircuitBreaker;
import backend.academy.resilience2.Retry;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class BotClientProxy implements BotClient {
    private BotClient delegate;

    @Retry
    @CircuitBreaker
    @Override
    public Mono<Void> sendUpdate(LinkUpdate linkUpdate) {
        return delegate.sendUpdate(linkUpdate);
    }

    public void switchStrategy(BotClient newDelegate) {
        delegate = newDelegate;
    }
}
