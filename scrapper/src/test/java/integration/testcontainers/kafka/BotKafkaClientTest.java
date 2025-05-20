package integration.testcontainers.kafka;

import backend.academy.configuration.EnvType;
import backend.academy.dto.LinkUpdate;
import backend.academy.scrapper.clients.BotKafkaClient;
import backend.academy.scrapper.repository.links.LinkRepository;
import backend.academy.scrapper.repository.links.ORMLinkRepository;
import integration.BaseTestcontainersTest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DirtiesContext
@ContextConfiguration(classes = BotKafkaClientTest.ConfigKafka.class)
public class BotKafkaClientTest extends BaseTestcontainersTest {

    private static final String TEST_TOPIC = "link-updates-test";

    @Autowired
    private BotKafkaClient botKafkaClient;

    @Autowired
    private NewTopic linkUpdatesTopic;

    private BlockingQueue<ConsumerRecord<String, LinkUpdate>> records;
    private KafkaMessageListenerContainer<String, LinkUpdate> container;

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.kafka.kafka-topics.link-updates.name", () -> TEST_TOPIC);
    }

    @BeforeEach
    void setUp() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        DefaultKafkaConsumerFactory<String, LinkUpdate> consumerFactory =
            new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(),
                new JsonDeserializer<>(LinkUpdate.class, false));

        ContainerProperties containerProperties = new ContainerProperties(TEST_TOPIC);
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, LinkUpdate>) records::add);
        container.start();

        ContainerTestUtils.waitForAssignment(container, linkUpdatesTopic.numPartitions());
    }

    @AfterEach
    void tearDown() {
        if (container != null) {
            container.stop();
        }
    }

    @Test
    void testSendUpdate() throws Exception {
        LinkUpdate linkUpdate = new LinkUpdate(
            1L,
            "https://github.com/user/repo",
            "Test description",
            List.of(1L, 2L)
        );

        Mono<Void> result = botKafkaClient.sendUpdate(linkUpdate);

        StepVerifier.create(result)
            .verifyComplete();

        ConsumerRecord<String, LinkUpdate> record = records.poll(10, TimeUnit.SECONDS);
        assertNotNull(record);
        assertEquals(TEST_TOPIC, record.topic());

        LinkUpdate receivedUpdate = record.value();
        assertEquals(linkUpdate.linkId(), receivedUpdate.linkId());
        assertEquals(linkUpdate.url(), receivedUpdate.url());
        assertEquals(linkUpdate.description(), receivedUpdate.description());
        assertEquals(linkUpdate.tgChatIds(), receivedUpdate.tgChatIds());
    }

    @Configuration
    static class ConfigKafka {
        @Bean
        public LinkRepository linkRepository(SessionFactory sessionFactory) {
            return new ORMLinkRepository(EnvType.TEST, sessionFactory);
        }
    }
}
