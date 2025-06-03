package backend.academy.scrapper.clients;

import backend.academy.dto.LinkUpdate;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class BotClientProxy implements BotClient {
    private BotClient delegate;

    @Override
    public Mono<Void> sendUpdate(LinkUpdate linkUpdate) {
        return delegate.sendUpdate(linkUpdate);
    }

    public void switchStrategy(BotClient newDelegate) {
        delegate = newDelegate;
    }
}
