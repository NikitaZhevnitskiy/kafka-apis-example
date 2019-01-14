package ru.zhenik.kafkaapis.admin;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.errors.TopicExistsException;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;


public class CreateTopics {
    public static void main(String[] args) {

        createTopic("topic-1", 5, (short)1);

    }

    public static void createTopic(final String topicName, final int partitions, final short replicationFactor) {
        // Create admin client
        try (final AdminClient adminClient = KafkaAdminClient.create(buildDefaultClientConfig())) {
            try {
                // Define topic
                final NewTopic newTopic = new NewTopic(topicName, partitions, replicationFactor);

                // Create topic, which is async call.
                final CreateTopicsResult createTopicsResult = adminClient.createTopics(Collections.singleton(newTopic));

                // Since the call is Async, Lets wait for it to complete.
                createTopicsResult.values().get(topicName).get();
            } catch (InterruptedException | ExecutionException e) {

                if (!(e.getCause() instanceof TopicExistsException)) {
                    throw new RuntimeException(e.getMessage(), e);
                }

                // TopicExistsException - Swallow this exception, just means the topic already exists.
                System.out.println("Topic : " + topicName + " already exists");
            }
        }
    }

    public static Properties buildDefaultClientConfig() {
        final Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(AdminClientConfig.CLIENT_ID_CONFIG, "admin-client-id1");
        properties.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 500);
        return properties;
    }
}
