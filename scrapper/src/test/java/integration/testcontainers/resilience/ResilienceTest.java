package integration.testcontainers.resilience;

import integration.BaseTestcontainersTest;
import integration.testcontainers.configuration.TestcontainersGenericConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ContextConfiguration(
        classes = {
            TestcontainersGenericConfiguration.class,
            ResilientTestConfig.class,
        })
@ExtendWith(MockitoExtension.class)
public class ResilienceTest extends BaseTestcontainersTest {
    @MockitoSpyBean
    private ResilientClient resilientClient;

    @MockitoSpyBean
    private ResilientEndpoint resilientEndpoint;

    private WebClient client;

    @BeforeEach
    void setUp() {
        String baseUrl = "http://localhost:" + port;
        client = WebClient.create(baseUrl);

        Mockito.reset(resilientClient, resilientEndpoint);
    }


    @Test
    public void testRetry() {
        try {
            resilientClient.doMonoRetry("stuff").block();
        } catch (Exception e) {
        }

        verify(resilientClient, times(5)).innerLogic();
    }

    @Test
    public void testRetryWithBlacklistedStatusCode() {
        try {
            resilientClient.doMonoNotRetry("stuff").block();
        } catch (Exception e) {
        }

        verify(resilientClient, times(1)).innerLogic();
    }

    @Test
    public void testCircuitBreaker() {
        for (int i = 0; i < 10; i++) {
            try {
                resilientClient.doMonoCircuitBreaker("stuff").block();
            } catch (Exception e) {
            }
        }

        int callCount = Mockito.mockingDetails(resilientClient).getInvocations().size();
        System.out.println("Circuit breaker test: innerLogic called " + callCount + " times");

        reset(resilientClient);

        try {
            resilientClient.doMonoCircuitBreaker("stuff").block();
        } catch (Exception e) {
        }

        verify(resilientClient, never()).innerLogic();
    }

    @Test
    public void testFallback() {
        String result = resilientClient.doMonoFallback("stuff").block();

        verify(resilientClient, times(1)).innerLogic();

        verify(resilientClient, times(1)).fallbackMethod(anyString());
        assert result.equals("stuff_afterError");
    }

    @Test
    public void testRateLimit() {
        for (int i = 0; i < 15; i++) {
            try {
                client.get().uri("/mono").retrieve().bodyToMono(String.class).block();
            } catch (Exception e) {
                if (i >= 10) {
                    assert e instanceof WebClientResponseException;
                    assert ((WebClientResponseException) e).getStatusCode().value() == 429;
                } else {
                    throw e; // Unexpected exception
                }
            }
        }

        verify(resilientEndpoint, atMost(15)).innerLogic();
    }


    @Test
    public void testRetryWithBreaker() {
        for (int i = 0; i < 5; i++) {
            try {
                resilientClient.doMonoRetryWithBreaker("stuff").block();
            } catch (Exception e) {
            }
        }

        int callCount = Mockito.mockingDetails(resilientClient).getInvocations().size();
        System.out.println("Retry with breaker test: innerLogic called " + callCount + " times");

        reset(resilientClient);

        try {
            resilientClient.doMonoRetryWithBreaker("stuff").block();
        } catch (Exception e) {
        }

        verify(resilientClient, never()).innerLogic();
    }

    @Test
    public void testFallbackWithRetry() {
        String result = resilientClient.doMonoFallbackWithRetry("stuff").block();

        verify(resilientClient, times(5)).innerLogic(); // 4 attempts (1 original + 3 retries)

        verify(resilientClient, times(1)).fallbackMethod(anyString());
        assert result.equals("stuff_afterError");
    }

    @Test
    public void testFallbackWithBreaker() {
        String result = resilientClient.doMonoFallbackWithBreaker("stuff").block();

        verify(resilientClient, times(1)).innerLogic();

        verify(resilientClient, times(1)).fallbackMethod(anyString());
        assert result.equals("stuff_afterError");

        for (int i = 0; i < 10; i++) {
            result = resilientClient.doMonoFallbackWithBreaker("stuff").block();
            assert result.equals("stuff_afterError");
        }

        reset(resilientClient);

        result = resilientClient.doMonoFallbackWithBreaker("stuff").block();

        verify(resilientClient, never()).innerLogic();

        verify(resilientClient, times(1)).fallbackMethod(anyString());
        assert result.equals("stuff_afterError");
    }


    @Test
    public void testMonoFull() {
        String result = resilientClient.doMonoFull("stuff").block();

        assert result.equals("stuff");

        verify(resilientClient, times(1)).innerLogic();
    }

    @Test
    public void testFluxFull() {
        String result = resilientClient.doFluxFull("stuff").blockFirst();

        assert result.equals("stuff");

        verify(resilientClient, times(1)).innerLogic();
    }
}
