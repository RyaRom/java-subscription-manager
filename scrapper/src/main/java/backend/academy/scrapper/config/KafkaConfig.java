package backend.academy.scrapper.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Log4j2
@Configuration
@RequiredArgsConstructor
public class KafkaConfig {
    private final KafkaProps kafkaProps;

    @Bean
    public NewTopic linkUpdatesTopic() {
        var kafkaTopics = kafkaProps.kafkaTopics();
        var topic = kafkaTopics.get("link-updates");
        return TopicBuilder.name(topic.getName())
                .partitions(topic.getPartitions())
                .replicas(topic.getReplicas())
                .configs(topic.getConfig())
                .build();
    }
}
