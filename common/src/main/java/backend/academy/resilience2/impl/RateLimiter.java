package backend.academy.resilience2.impl;

public interface RateLimiter {
    boolean processRequest();
}
