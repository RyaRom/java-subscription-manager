package backend.academy.resilience2.impl;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import backend.academy.configuration.ResilienceProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
public class InMemoryTokenBucketRateLimiter implements RateLimiter {
    private final ResilienceProps.RateLimiter rateLimiterProps;
    // possible redis or other shared storage implementation
    private final Map<String, WeakReference<TokenBucket>> tokenBucketsForIp = new ConcurrentHashMap<>();

    @Override
    public int processRequest(String userIp) {
        TokenBucket tokenBucket = tokenBucketsForIp.computeIfAbsent(
                userIp,
                k -> new WeakReference<>(new TokenBucket(
                        new AtomicInteger(rateLimiterProps.maxTokens()), new AtomicLong(System.currentTimeMillis())))).get();
        if (tokenBucket == null) {
            tokenBucketsForIp.put(userIp, new WeakReference<>(new TokenBucket(
                    new AtomicInteger(rateLimiterProps.maxTokens()), new AtomicLong(System.currentTimeMillis()))));
            tokenBucket = tokenBucketsForIp.get(userIp).get();
        }
        if (tokenBucket.refill(rateLimiterProps.tokensPerSecond(), rateLimiterProps.maxTokens())) {
            return -1;
        } else {
            int elapsedMs = (int) (System.currentTimeMillis() - tokenBucket.lastRefillTimeMs.get());
            int msPerToken = 1000 / rateLimiterProps.tokensPerSecond();
            return msPerToken - elapsedMs;
        }
    }

    private record TokenBucket(AtomicInteger tokensLeft, AtomicLong lastRefillTimeMs) {
        public boolean refill(int tokensPerSecond, int maxTokens) {
            log.info(
                    "TokenBucket refill - tokensLeft={}, lastRefillTimeMs={}",
                    tokensLeft.get(),
                    lastRefillTimeMs.get());
            long now = System.currentTimeMillis();
            long lastTime = lastRefillTimeMs.get();
            double elapsedSec = (now - lastTime) / 1000f;
            int newTokens = (int) (tokensPerSecond * elapsedSec);
            int current, updated;
            do {
                current = tokensLeft.get();
                updated = Math.min(current + newTokens, maxTokens);
                log.info("TokenBucket update - current={}, newTokens={}, updated={}", current, newTokens, updated);

                if (updated <= 0) {
                    return false;
                }
            } while (!tokensLeft.compareAndSet(current, updated - 1));
            log.info("TOKENS LEFT: {}", tokensLeft.get());

            lastRefillTimeMs.compareAndSet(lastTime, System.currentTimeMillis());
            return true;
        }
    }
}
