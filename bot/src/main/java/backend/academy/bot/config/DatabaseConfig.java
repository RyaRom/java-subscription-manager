package backend.academy.bot.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;

@Validated
@Profile({"dev"})
@ConfigurationProperties(prefix = "app.data", ignoreUnknownFields = false)
public record DatabaseConfig(
    @NotEmpty String redis
) {
    @Bean
    public RedisClient redisClient() {
        return RedisClient.create(redis);
    }

//    @Bean
//    public StatefulRedisConnection<String, Object> statefulRedisConnection(
//        RedisClient redisClient
//    ) {
//        return redisClient.connect()
//    }
}
