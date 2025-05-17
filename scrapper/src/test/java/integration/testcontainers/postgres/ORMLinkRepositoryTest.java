package integration.testcontainers.postgres;

import backend.academy.configuration.EnvType;
import backend.academy.scrapper.repository.links.LinkRepository;
import backend.academy.scrapper.repository.links.ORMLinkRepository;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = ORMLinkRepositoryTest.ConfigOrmRepo.class)
public class ORMLinkRepositoryTest extends LinkRepositoryTest {
    @Autowired
    private ORMLinkRepository linkRepository;

    @Override
    protected LinkRepository getLinkRepository() {
        return linkRepository;
    }

    @Configuration
    static class ConfigOrmRepo {
        @Bean
        public LinkRepository linkRepository(SessionFactory sessionFactory) {
            return new ORMLinkRepository(EnvType.TEST, sessionFactory);
        }
    }
}
