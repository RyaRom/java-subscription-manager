package integration.testcontainers.resilience;

import integration.BaseTestcontainersTest;
import integration.testcontainers.configuration.TestcontainersGenericConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.reactive.function.client.WebClient;

@ContextConfiguration(classes = {
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
    }

    @Test
    public void testMono() {
        resilientClient.doMonoFull("stuff").subscribe();
    }
}
