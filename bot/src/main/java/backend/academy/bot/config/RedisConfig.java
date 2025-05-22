package backend.academy.bot.config;

import backend.academy.proto.impl.Links;
import backend.academy.service.RedisProtoCodec;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Profile({"dev"})
@Log4j2
@Configuration
@RequiredArgsConstructor
public class RedisConfig {
    private final DataProps dataProps;

    @Bean
    public RedisClient redisClient() {
        return RedisClient.create(dataProps.redis());
    }

    @Bean
    public StatefulRedisConnection<String, Links.ListLinksProto> statefulRedisConnection(
        RedisClient redisClient
    ) {
        return redisClient.connect(new RedisProtoCodec<>(
            Links.ListLinksProto.class
        ));
    }

    @Bean
    public RedisReactiveCommands<String, Links.ListLinksProto> redisReactiveCommandsProto(
        StatefulRedisConnection<String, Links.ListLinksProto> statefulRedisConnection
    ) {
        return statefulRedisConnection.reactive();
    }

    @Bean
    RedisReactiveCommands<String, String> redisReactiveCommandsString(
        RedisClient redisClient
    ) {
        return redisClient.connect().reactive();
    }
}
