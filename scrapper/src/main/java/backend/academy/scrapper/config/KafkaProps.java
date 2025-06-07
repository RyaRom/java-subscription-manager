package backend.academy.scrapper.config;

import jakarta.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Log4j2
@ConfigurationProperties(prefix = "spring.kafka")
public record KafkaProps(Map<String, Topic> kafkaTopics) {
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
