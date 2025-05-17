package integration.testcontainers.postgres;

import backend.academy.configuration.EnvType;
import backend.academy.scrapper.repository.links.LinkRepository;
import backend.academy.scrapper.repository.links.SQLLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = SQLLinkRepositoryTest.ConfigSqlRepo.class)
public class SQLLinkRepositoryTest extends LinkRepositoryTest {
    @Autowired
    private SQLLinkRepository linkRepository;

    @Override
    protected LinkRepository getLinkRepository() {
        return linkRepository;
    }

    @Configuration
    static class ConfigSqlRepo {
        @Bean
        public LinkRepository linkRepository() {
            return new SQLLinkRepository(EnvType.TEST, postgresContainer.getJdbcUrl(),
                postgresContainer.getUsername(), postgresContainer.getPassword());
        }
    }
}
