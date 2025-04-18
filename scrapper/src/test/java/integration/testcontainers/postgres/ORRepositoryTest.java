package integration.testcontainers.postgres;

import integration.BaseTestcontainersTest;
import org.junit.jupiter.api.Test;

public class ORRepositoryTest extends BaseTestcontainersTest {
    @Test
    void name() {
        System.out.println(postgresContainer.getDatabaseName());
    }
}
