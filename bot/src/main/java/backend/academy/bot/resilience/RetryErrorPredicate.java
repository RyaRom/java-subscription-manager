package backend.academy.bot.resilience;

import java.util.function.Predicate;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Log4j2
public class RetryErrorPredicate implements Predicate<Throwable> {
    @Override
    public boolean test(Throwable e) {
        log.info("Got error {} : {} in request", e.getMessage(), e);
        if (e instanceof WebClientResponseException responseException) {
            log.info("Error {} status {}",
                responseException.getMessage(),
                responseException.getStatusCode());
            return responseException.getStatusCode().is5xxServerError()
                || responseException.getStatusCode().value() == 429;
        }
        return e instanceof WebClientRequestException;
    }
}
