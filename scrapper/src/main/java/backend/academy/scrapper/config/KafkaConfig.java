package backend.academy.scrapper.config;

import jakarta.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

@Log4j2
@ConfigurationProperties(prefix = "spring.kafka")
public record KafkaConfig(Map<String, Topic> kafkaTopics) {

    @Bean
    public NewTopic linkUpdatesTopic() {
        var topic = kafkaTopics.get("link-updates");
        return TopicBuilder.name(topic.getName())
                .partitions(topic.getPartitions())
                .replicas(topic.getReplicas())
                .configs(topic.getConfig())
                .build();
    }

    @Bean
    public NewTopic linkUpdatesDLQTopic() {
        var topic = kafkaTopics.get("dlq");
        return TopicBuilder.name(topic.getName())
                .partitions(topic.getPartitions())
                .replicas(topic.getReplicas())
                .configs(topic.getConfig())
                .build();
    }

    @Getter
    @Setter
    public static class Topic {
        private String name;

        @Nullable
        private Integer partitions;

        @Nullable
        private Integer replicas;

        @Nullable
        private Map<String, String> config;

        public int getPartitions() {
            return partitions == null ? 1 : partitions;
        }

        public int getReplicas() {
            return replicas == null ? 1 : replicas;
        }

        public Map<String, String> getConfig() {
            return config == null ? new HashMap<>() : config;
        }
    }
}
