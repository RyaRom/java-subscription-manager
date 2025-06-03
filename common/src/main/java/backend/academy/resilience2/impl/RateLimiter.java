package backend.academy.resilience2.impl;

public interface RateLimiter {
    /** @return retry-after in ms, or -1 if was allowed */
    int processRequest(String userIp);
}
