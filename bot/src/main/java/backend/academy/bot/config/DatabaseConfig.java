package backend.academy.bot.config;

import backend.academy.proto.impl.Links.ListLinksProto;
import backend.academy.service.RedisProtoCodec;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;

@Validated
@Profile({"dev"})
@Log4j2
@ConfigurationProperties(prefix = "app.data", ignoreUnknownFields = false)
public record DatabaseConfig(
    @NotEmpty String redis
) {
    @Bean
    public RedisClient redisClient() {
        return RedisClient.create(redis);
    }

    @Bean
    public StatefulRedisConnection<String, ListLinksProto> statefulRedisConnection(
        RedisClient redisClient
    ) {
        return redisClient.connect(new RedisProtoCodec<>(
            ListLinksProto.class
        ));
    }

    @Bean
    public RedisReactiveCommands<String, ListLinksProto> redisReactiveCommands(
        StatefulRedisConnection<String, ListLinksProto> statefulRedisConnection
    ) {
        return statefulRedisConnection.reactive();
    }
}
