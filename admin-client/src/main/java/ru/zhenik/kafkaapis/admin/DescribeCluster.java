package ru.zhenik.kafkaapis.admin;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.common.Node;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

public class DescribeCluster {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        cluster();
    }

    public static void cluster() throws ExecutionException, InterruptedException {
        // Create admin client
        try (final AdminClient adminClient = KafkaAdminClient.create(Util.buildDefaultClientConfig())) {
            final DescribeClusterResult describeClusterResult = adminClient.describeCluster();
            // brokers in cluster
            final Collection<Node> brokers = describeClusterResult.nodes().get();


        }
    }


}
