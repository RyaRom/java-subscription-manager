package backend.academy.resilience2.impl;

public class ImMemoryTokenBucketRateLimiter implements RateLimiter{
    @Override
    public boolean processRequest() {
        return false;
    }
}
