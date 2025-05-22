package backend.academy.scrapper.config;

import backend.academy.proto.impl.LinkEntities;
import backend.academy.service.RedisProtoCodec;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {
    private final DataConnectionProperties dataConnectionProperties;

    @Bean
    public RedisClient redisClient() {
        return RedisClient.create(dataConnectionProperties.redis());
    }

    @Bean
    public StatefulRedisConnection<String, LinkEntities.FullLinkProto> statefulRedisConnection(
            RedisClient redisClient) {
        return redisClient.connect(new RedisProtoCodec<>(LinkEntities.FullLinkProto.class));
    }

    @Bean
    public RedisAsyncCommands<String, LinkEntities.FullLinkProto> redisAsyncCommands(
            StatefulRedisConnection<String, LinkEntities.FullLinkProto> statefulRedisConnection) {
        return statefulRedisConnection.async();
    }
}
