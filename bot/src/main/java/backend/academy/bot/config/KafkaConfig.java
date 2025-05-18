package backend.academy.bot.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ConsumerAwareListenerErrorHandler;

@Log4j2
@Configuration
public class KafkaConfig {
    @Bean
    public ConsumerAwareListenerErrorHandler kafkaErrorHandler() {
        return (message, exception, consumer) -> {
            log.error("Error processing message: {}", String.valueOf(message.getPayload()), exception);
            return "";
        };
    }
}
