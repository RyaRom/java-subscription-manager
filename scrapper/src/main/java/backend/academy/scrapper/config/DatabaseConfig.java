package backend.academy.scrapper.config;

import backend.academy.configuration.EnvType;
import backend.academy.proto.impl.LinkEntities;
import backend.academy.scrapper.repository.links.CachedLinkRepository;
import backend.academy.scrapper.repository.links.LinkRepository;
import backend.academy.scrapper.repository.links.ORMLinkRepository;
import backend.academy.scrapper.repository.links.SQLLinkRepository;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.apache.logging.log4j.core.config.ConfigurationException;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig {

    @Bean
    public LinkRepository linkRepository(
            EnvType envType,
            DataConnectionProperties dataConnectionProperties,
            SessionFactory sessionFactory,
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password,
            RedisAsyncCommands<String, LinkEntities.FullLinkProto> redisAsyncCommands) {
        if (dataConnectionProperties.type().equalsIgnoreCase("orm")) {
            return new CachedLinkRepository(
                    dataConnectionProperties, redisAsyncCommands, new ORMLinkRepository(envType, sessionFactory));
        }
        if (dataConnectionProperties.type().equalsIgnoreCase("sql")) {
            return new CachedLinkRepository(
                    dataConnectionProperties,
                    redisAsyncCommands,
                    new SQLLinkRepository(envType, url, username, password));
        }
        throw new ConfigurationException("data.type should be sql or orm");
    }
}
