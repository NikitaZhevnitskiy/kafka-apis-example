package ru.zhenik.kafkaapis.admin;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.errors.TopicExistsException;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutionException;


public class CreateTopics {
    public static void main(String[] args) {
        createTopic("topic-1", 5, (short) 1);
        createTopicVerifyCluster("topic-failt-to-create", 5, (short) 2);

    }

    /**
     * If amount of brokers less than given replication factor,
     * than while topic creation given replication will be ignored and set to 1
     */
    public static void createTopic(final String topicName, final int partitions, final short replicationFactor) {
        // Create admin client
        try (final AdminClient adminClient = KafkaAdminClient.create(Util.buildDefaultClientConfig())) {
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

    public static void createTopicVerifyCluster(final String topicName, final int partitions, final short replicationFactor) {
        // Create admin client
        try (final AdminClient adminClient = KafkaAdminClient.create(Util.buildDefaultClientConfig())) {
            try {

                // verify
                final Collection<Node> nodes = adminClient.describeCluster().nodes().get();
                if (nodes.size() < replicationFactor) {
                    throw new IllegalArgumentException(
                            String.format("Amount of brokers in cluster lower than replication factor nodes:replicationFactor[%s: %s]",
                                    nodes.size() + "", replicationFactor + ""));
                }

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


}
