package backend.academy.scrapper.config;

import backend.academy.scrapper.repository.InMemoryLinkRepository;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.ORMLinkRepository;
import backend.academy.scrapper.rest.AdminController;
import jakarta.validation.constraints.NotEmpty;
import org.apache.logging.log4j.core.config.ConfigurationException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;

@Validated
@Profile("dev")
@ConfigurationProperties(prefix = "app.data", ignoreUnknownFields = false)
@Import({
    AdminController.class
})
public record DevConfig(@NotEmpty String type) {
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
