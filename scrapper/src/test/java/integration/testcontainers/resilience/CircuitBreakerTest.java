package integration.testcontainers.resilience;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import backend.academy.configuration.ResilienceProps;
import backend.academy.resilience2.CircuitBreakerAspect;
import integration.BaseTestcontainersTest;
import integration.testcontainers.configuration.TestcontainersGenericConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@ContextConfiguration(
        classes = {
            TestcontainersGenericConfiguration.class,
            ResilientTestConfig.class,
        })
@ExtendWith(MockitoExtension.class)
public class CircuitBreakerTest extends BaseTestcontainersTest {
    public static final String SUCCESS = "hiii:3";

    @Autowired
    private ResilienceProps resilienceProps;

    @Autowired
    private CircuitBreakerAspect circuitBreakerAspect;

    private ResilienceProps.CircuitBreaker circuitBreakerProps;

    @MockitoSpyBean
    private ResilientClient resilientClient;

    @MockitoSpyBean
    private ResilientEndpoint resilientEndpoint;

    @BeforeEach
    void setUp() {
        circuitBreakerAspect.flush();
        circuitBreakerProps = resilienceProps.circuitBreaker();
        Mockito.reset(resilientClient, resilientEndpoint);
    }

    /** Test normal operation with circuit breaker in CLOSED state */
    @Test
    public void testCircuitBreakerClosed() {
        String result = resilientClient.doMonoCircuitBreakerConditional(false).block();

        assert SUCCESS.equals(result);

        verify(resilientClient, times(1)).innerLogic();
    }

    /** Test circuit breaker transitioning to OPEN state after failures */
    @Test
    public void testCircuitBreakerOpensAfterFailures() {
        openCb();

        reset(resilientClient);

        try {
            resilientClient.doMonoCircuitBreakerConditional(true).block();
        } catch (Exception e) {
            assert e instanceof WebClientResponseException;
            assert ((WebClientResponseException) e).getStatusCode().value() == 429;
        }

        verify(resilientClient, never()).innerLogic();
    }

    /** Test circuit breaker in HALF-OPEN state after wait duration */
    @Test
    public void testCircuitBreakerHalfOpenAfterWaitDuration() throws InterruptedException {
        openCb();

        reset(resilientClient);

        try {
            resilientClient.doMonoCircuitBreakerConditional(false).block();
        } catch (Exception e) {
            assert e instanceof WebClientResponseException;
            assert ((WebClientResponseException) e).getStatusCode().value() == 429;
        }

        Thread.sleep(circuitBreakerProps.waitDurationInOpenStateMs() * 2);

        try {
            String result =
                    resilientClient.doMonoCircuitBreakerConditional(false).block();
            assert SUCCESS.equals(result);
            verify(resilientClient, times(1)).innerLogic();
        } catch (Exception e) {
            assert false : "Should not throw exception in HALF-OPEN state for successful requests";
        }
    }

    /** Test circuit breaker transitioning back to CLOSED state after successful requests in HALF-OPEN state */
    @Test
    public void testCircuitBreakerClosesAfterSuccessInHalfOpen() throws InterruptedException {
        openCb();

        Thread.sleep(circuitBreakerProps.waitDurationInOpenStateMs() * 2);

        for (int i = 0; i < toOpen(); i++) {
            try {
                String result =
                        resilientClient.doMonoCircuitBreakerConditional(false).block();
                assert SUCCESS.equals(result);
            } catch (Exception e) {
                assert false : "Should not throw exception in HALF-OPEN state for successful requests";
            }
        }

        reset(resilientClient);

        String result = resilientClient.doMonoCircuitBreakerConditional(false).block();
        assert SUCCESS.equals(result);
        verify(resilientClient, times(1)).innerLogic();
    }

    /** Test circuit breaker transitioning back to OPEN state after failed requests in HALF-OPEN state */
    @Test
    public void testCircuitBreakerOpensAfterFailureInHalfOpen() throws InterruptedException {
        openCb();

        Thread.sleep(circuitBreakerProps.waitDurationInOpenStateMs() * 2);

        try {
            resilientClient.doMonoCircuitBreakerConditional(true).block();
        } catch (Exception e) {
        }

        reset(resilientClient);

        try {
            resilientClient.doMonoCircuitBreakerConditional(false).block();
        } catch (Exception e) {
            assert e instanceof WebClientResponseException;
            assert ((WebClientResponseException) e).getStatusCode().value() == 429;
        }

        verify(resilientClient, never()).innerLogic();
    }

    /** Test circuit breaker with mixed success/failure patterns */
    @Test
    public void testCircuitBreakerWithMixedPattern() {
        for (int i = 0; i < toOpen() * 3; i++) {
            String result =
                    resilientClient.doMonoCircuitBreakerConditional(false).block();
            assert SUCCESS.equals(result);
        }

        for (int i = 0; i < toOpen(); i++) {
            try {
                resilientClient.doMonoCircuitBreakerConditional(true).block();
            } catch (Exception e) {
            }
        }

        reset(resilientClient);

        String result = resilientClient.doMonoCircuitBreakerConditional(false).block();
        assert SUCCESS.equals(result);
        verify(resilientClient, times(1)).innerLogic();

        openCb();
        openCb();
        openCb();

        reset(resilientClient);

        try {
            resilientClient.doMonoCircuitBreakerConditional(false).block();
        } catch (Exception e) {
            assert e instanceof WebClientResponseException;
            assert ((WebClientResponseException) e).getStatusCode().value() == 429;
        }

        verify(resilientClient, never()).innerLogic();
    }

    private void openCb() {
        for (int i = 0; i < toOpen(); i++) {
            try {
                resilientClient.doMonoCircuitBreakerConditional(true).block();
            } catch (Exception e) {
            }
        }
    }

    private void initCbClosed() {
        for (int i = 0; i < circuitBreakerProps.initCalls(); i++) {
            try {
                resilientClient.doMonoCircuitBreakerConditional(false).block();
            } catch (Exception e) {
            }
        }
    }

    private int toOpen() {
        return circuitBreakerProps.initCalls() * 2;
    }
}
