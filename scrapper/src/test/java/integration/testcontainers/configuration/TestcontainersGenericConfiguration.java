package integration.testcontainers.configuration;

import backend.academy.configuration.EnvType;
import backend.academy.scrapper.repository.links.LinkRepository;
import backend.academy.scrapper.repository.links.ORMLinkRepository;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestcontainersGenericConfiguration {
    @Bean
    @Primary
    public LinkRepository linkRepository(SessionFactory sessionFactory) {
        return new ORMLinkRepository(EnvType.TEST, sessionFactory);
    }
}
