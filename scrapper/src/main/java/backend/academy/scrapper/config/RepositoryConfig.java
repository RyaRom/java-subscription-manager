package backend.academy.scrapper.config;

import backend.academy.configuration.EnvType;
import backend.academy.scrapper.repository.InMemoryLinkRepository;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.ORMLinkRepository;
import jakarta.validation.constraints.NotEmpty;
import org.apache.logging.log4j.core.config.ConfigurationException;
import org.hibernate.SessionFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.context.annotation.RequestScope;

@Validated
@ConfigurationProperties(prefix = "app.data", ignoreUnknownFields = false)
public record RepositoryConfig(@NotEmpty String type) {
    @Bean
    @RequestScope
    public ORMLinkRepository ormLinkRepository(
        SessionFactory sessionFactory,
        EnvType envType
    ) {
        return new ORMLinkRepository(sessionFactory.openSession(), envType);
    }

    @Bean
    @RequestScope
    public LinkRepository linkRepository(
        ORMLinkRepository ormLinkRepository,
        InMemoryLinkRepository inMemoryLinkRepository
    ){
        if (type.equalsIgnoreCase("orm")){
            return ormLinkRepository;
        }
        if (type.equalsIgnoreCase("sql")){
            return inMemoryLinkRepository;
        }
        throw new ConfigurationException("data.type should be sql or orm");
    }
}
