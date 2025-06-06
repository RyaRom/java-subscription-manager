package backend.academy.resilience2.impl;

import backend.academy.configuration.ResilienceProps;
import backend.academy.resilience2.utils.ResilienceUtils;
import java.time.Instant;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
public class CountBasedCircuitBreaker implements CircuitBreaker {
    private final ResilienceProps.CircuitBreaker circuitBreakerProps;
    private State state;
    private int initCalls;
    private int callsInWindow;
    private int failedCalls;
    private int halfOpenCalls;
    private Instant openingTime;

    public CountBasedCircuitBreaker(ResilienceProps.CircuitBreaker circuitBreakerProps) {
        this.circuitBreakerProps = circuitBreakerProps;
        this.initCalls = circuitBreakerProps.initCalls();
        state = State.CLOSED;
        callsInWindow = 0;
        failedCalls = 0;
        halfOpenCalls = 0;
        openingTime = Instant.MIN;
    }

    @Override
    public boolean process() {
        log.info(
                "Request is in cb," + " state: {}, calls in window: {}, init calls: {},"
                        + " failed calls: {}, half open calls: {}, opening time: {}",
                state,
                callsInWindow,
                initCalls,
                failedCalls,
                halfOpenCalls,
                openingTime);
        if (initCalls > 0) {
            callsInWindow++;
            initCalls--;
            log.info("Calls before cb startup: {}", initCalls);
            return true;
        }

        if (state.equals(State.OPEN)) {
            if (timeSinceOpen() > circuitBreakerProps.waitDurationInOpenStateMs()) {
                halfOpenCb();
                return true;
            } else {
                log.info("Request is blocked by circuit breaker OPEN");
                return false;
            }
        }

        if (state.equals(State.HALF_OPEN)) {
            if (halfOpenCalls < 0) {
                openCb();
                log.info("Request is blocked by circuit breaker transitioning");
                return false;
            }
            if (halfOpenCalls > circuitBreakerProps.halfOpenCallsPermitted()) {
                closeCb();
            } else {
                halfOpenCalls++;
            }

            return true;
        }

        if (state.equals(State.CLOSED)) {
            if (errorRate() > circuitBreakerProps.failureRateThreshold()) {
                openCb();
                log.info("Request is blocked by circuit breaker transitioning");
                return false;
            }
            if (callsInWindow >= circuitBreakerProps.windowSize()) {
                rerollWindow();
            }
            callsInWindow++;

            return true;
        }
        return true;
    }

    private int errorRate() {
        if (failedCalls == 0) {
            return 0;
        }
        return (100 * failedCalls) / callsInWindow;
    }

    private long timeSinceOpen() {
        return 1000 * (Instant.now().getEpochSecond() - openingTime.getEpochSecond());
    }

    private void rerollWindow() {
        log.info("Rolling window");
        initCalls = circuitBreakerProps.initCalls();
        callsInWindow = 0;
        failedCalls = 0;
        halfOpenCalls = 0;
    }

    private void halfOpenCb() {
        log.info("cb transitioning from {} to HALF_OPEN state", state);
        state = State.HALF_OPEN;
        halfOpenCalls = 1;
    }

    private void closeCb() {
        log.info("cb transitioning from {} to CLOSED state", state);
        state = State.CLOSED;
        callsInWindow = 1;
        failedCalls = 0;
        halfOpenCalls = 0;
    }

    private void openCb() {
        log.info("cb transitioning from {} to OPEN state", state);
        state = State.OPEN;
        halfOpenCalls = 0;
        openingTime = Instant.now();
    }

    @Override
    public Mono<?> addOnErrorCallback(Mono<?> chain) {
        return chain.doOnError(this::errorCallback);
    }

    @Override
    public Flux<?> addOnErrorCallback(Flux<?> chain) {
        return chain.doOnError(this::errorCallback);
    }

    private void errorCallback(Throwable e) {
        if (initCalls > 0) {
            return;
        }

        log.info("In cb callback, failed requests per window: {}, error {}", failedCalls, e);
        if (ResilienceUtils.is4xx(e)) {
            log.info("4xx error in cb callback");
            return;
        }
        if (state.equals(State.CLOSED)) {
            log.info("Registering error for closed cb");
            failedCalls++;
            return;
        }
        if (state.equals(State.HALF_OPEN)) {
            log.info("Registering error for half open cb");
            halfOpenCalls = -1;
        }
    }
}
