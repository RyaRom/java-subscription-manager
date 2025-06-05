package backend.academy.scrapper.clients;

import backend.academy.dto.LinkUpdate;
import backend.academy.resilience2.CircuitBreaker;
import backend.academy.resilience2.Fallback;
import backend.academy.resilience2.RateLimit;
import backend.academy.resilience2.Retry;
import backend.academy.scrapper.resilience.RetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Log4j2
@SuppressWarnings("VA_FORMAT_STRING_USES_NEWLINE")
public class BotHttpClient implements BotClient {
    private final WebClient botWebClient;
    private final RetryService retryService;
    private final ApplicationContext applicationContext;

        @Override
        @Retry
        @Fallback("switchToKafka")
        @RateLimit
        @CircuitBreaker
        public Mono<Void> sendUpdate(LinkUpdate linkUpdate) {
            return retryService.withRetry(botWebClient
                    .post()
                    .uri("/updates")
                    .body(BodyInserters.fromValue(linkUpdate))
                    .retrieve()
                    .toBodilessEntity()
                    .then());
        }

    public Mono<Void> switchToKafka(LinkUpdate linkUpdate) {
        var proxy = applicationContext.getBean(BotClientProxy.class);
        var kafkaClient = applicationContext.getBean(BotKafkaClient.class);
        proxy.switchStrategy(kafkaClient);
        return kafkaClient.sendUpdate(linkUpdate);
    }
}
