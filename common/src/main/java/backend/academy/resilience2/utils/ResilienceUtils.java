package backend.academy.resilience2.utils;

import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@UtilityClass
public class ResilienceUtils {
    public static @NotNull WebClientResponseException getTooManyRequests(int retryAfterMs) {
        return new WebClientResponseException(
                429,
                "Too Many Requests",
                new HttpHeaders(MultiValueMap.fromMultiValue(
                        Map.of(HttpHeaders.RETRY_AFTER, List.of(String.valueOf(retryAfterMs))))),
                null,
                null);
    }

    public static boolean is4xx(Throwable throwable) {
        return throwable instanceof WebClientResponseException
                && ((WebClientResponseException) throwable).getStatusCode().is4xxClientError();
    }
}
