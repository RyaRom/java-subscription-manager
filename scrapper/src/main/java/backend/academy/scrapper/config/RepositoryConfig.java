package backend.academy.scrapper.config;

import backend.academy.scrapper.repository.InMemoryLinkRepository;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.ORMLinkRepository;
import jakarta.validation.constraints.NotEmpty;
import org.apache.logging.log4j.core.config.ConfigurationException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.data", ignoreUnknownFields = false)
public record RepositoryConfig(@NotEmpty String type) {
    @Bean
    public LinkRepository linkRepository(
        ORMLinkRepository ormLinkRepository,
        InMemoryLinkRepository inMemoryLinkRepository
    ) {
        if (type.equalsIgnoreCase("orm")) {
            return ormLinkRepository;
        }
        if (type.equalsIgnoreCase("sql")) {
            return inMemoryLinkRepository;
        }
        throw new ConfigurationException("data.type should be sql or orm");
    }
}
