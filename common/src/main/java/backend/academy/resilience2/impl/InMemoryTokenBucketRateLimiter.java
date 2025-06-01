package backend.academy.resilience2.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import backend.academy.configuration.ResilienceProps;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InMemoryTokenBucketRateLimiter implements RateLimiter {
    private final ResilienceProps resilienceProps;
    //possible redis or other shared storage implementation
    private final Map<String, TokenBucket> tokenBuckets = new ConcurrentHashMap<>();

    @Override
    public int processRequest(String userIp) {
        return 1;
    }


    @Data
    private static class TokenBucket {
        private int tokensLeft;
        private long lastRefillTime;
    }
}
