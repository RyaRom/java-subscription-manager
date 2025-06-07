package integration.testcontainers.resilience;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    ResilientClient.class,
    ResilientEndpoint.class,
})
public class ResilientTestConfig {
}
