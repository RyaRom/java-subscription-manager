package backend.academy.scrapper.config;

import backend.academy.configuration.EnvType;
import backend.academy.scrapper.repository.links.LinkRepository;
import backend.academy.scrapper.repository.links.ORMLinkRepository;
import backend.academy.scrapper.repository.links.SQLLinkRepository;
import backend.academy.scrapper.rest.AdminController;
import jakarta.validation.constraints.NotEmpty;
import org.apache.logging.log4j.core.config.ConfigurationException;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;

@Validated
@Profile({"dev"})
@ConfigurationProperties(prefix = "app.data", ignoreUnknownFields = false)
@Import({
    AdminController.class
})
public record DatabaseConfig(@NotEmpty String type) {
    @Bean
    public LinkRepository linkRepository(
        EnvType envType,
        SessionFactory sessionFactory,
        @Value("${spring.datasource.url}")
        String url,
        @Value("${spring.datasource.username}")
        String username,
        @Value("${spring.datasource.password}")
        String password
    ) {
        if (type.equalsIgnoreCase("orm")) {
            return new ORMLinkRepository(envType, sessionFactory);
        }
        if (type.equalsIgnoreCase("sql")) {
            return new SQLLinkRepository(envType, url, username, password);
        }
        throw new ConfigurationException("data.type should be sql or orm");
    }
}
