package backend.academy.scrapper.config;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

@Log4j2
@ConfigurationProperties(prefix = "spring.kafka")
public record KafkaConfig(
        Map<String, Topic> kafkaTopics
) {

    @Bean
    public NewTopic linkUpdatesTopic() {
        var topic = kafkaTopics.get("link-updates");
        return TopicBuilder.name(topic.name)
                .partitions(topic.partitions)
                .replicas(topic.replicas)
                .configs(topic.config)
                .build();
    }

    @lombok.Value
    public static class Topic {
        String name;
        int partitions = 1;
        short replicas = 1;
        Map<String, String> config = new HashMap<>();
    }
}
