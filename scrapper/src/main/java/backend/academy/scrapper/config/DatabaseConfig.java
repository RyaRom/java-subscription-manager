package backend.academy.scrapper.config;

import backend.academy.configuration.EnvType;
import backend.academy.proto.impl.LinkEntities;
import backend.academy.scrapper.repository.links.CachedLinkRepository;
import backend.academy.scrapper.repository.links.LinkRepository;
import backend.academy.scrapper.repository.links.ORMLinkRepository;
import backend.academy.scrapper.repository.links.SQLLinkRepository;
import backend.academy.scrapper.rest.AdminController;
import backend.academy.service.RedisProtoCodec;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.core.config.ConfigurationException;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Profile({"dev"})
@Import({AdminController.class})
@Configuration
@RequiredArgsConstructor
public class DatabaseConfig {
    private final DataConnectionProperties dataConnectionProperties;

    @Bean
    public LinkRepository linkRepository(
        EnvType envType,
        DataConnectionProperties dataConnectionProperties,
        SessionFactory sessionFactory,
        @Value("${spring.datasource.url}") String url,
        @Value("${spring.datasource.username}") String username,
        @Value("${spring.datasource.password}") String password,
        RedisAsyncCommands<String, LinkEntities.FullLinkProto> redisAsyncCommands
    ) {
        if (dataConnectionProperties.type().equalsIgnoreCase("orm")) {
            return new CachedLinkRepository(
                dataConnectionProperties,
                redisAsyncCommands,
                new ORMLinkRepository(envType, sessionFactory)
            );
        }
        if (dataConnectionProperties.type().equalsIgnoreCase("sql")) {
            return new CachedLinkRepository(
                dataConnectionProperties,
                redisAsyncCommands,
                new SQLLinkRepository(envType, url, username, password)
            );
        }
        throw new ConfigurationException("data.type should be sql or orm");
    }

    @Bean
    public RedisClient redisClient() {
        return RedisClient.create(dataConnectionProperties.redis());
    }

    @Bean(destroyMethod = "close")
    public StatefulRedisConnection<String, LinkEntities.FullLinkProto> statefulRedisConnection(
        RedisClient redisClient
    ) {
        return redisClient.connect(new RedisProtoCodec<>(
            LinkEntities.FullLinkProto.class
        ));
    }

    @Bean
    public RedisAsyncCommands<String, LinkEntities.FullLinkProto> redisAsyncCommands(
        StatefulRedisConnection<String, LinkEntities.FullLinkProto> statefulRedisConnection
    ) {
        return statefulRedisConnection.async();
    }
}
