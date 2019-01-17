package ru.zhenik.kafkaapis.consumer;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import ru.zhenik.kafkaapis.schema.avro.Words;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

public class ConsumerExplicitCommit implements Runnable {

    private final String consumerId;
    private final String consumerGroupId;
    private final String topic;
    private final Properties properties;
    private final KafkaConsumer<String, Words> kafkaSpecificConsumer;

    public ConsumerExplicitCommit(final String topic, final Properties properties) {
        this.topic=topic;
        this.properties = properties;
        // disable autocommit
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        this.consumerId=properties.getProperty(ConsumerConfig.CLIENT_ID_CONFIG);
        this.consumerGroupId=properties.getProperty(ConsumerConfig.GROUP_ID_CONFIG);
        this.kafkaSpecificConsumer=new KafkaConsumer<String, Words>(properties);
    }

    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        kafkaSpecificConsumer.subscribe(Collections.singletonList(topic));
        while (true) {
            final ConsumerRecords<String, Words> records = kafkaSpecificConsumer.poll(Duration.ofMillis(100));
            for (final ConsumerRecord<String, Words> record : records) {
                procesing(record);
            }
        }
    }

    void procesing(final ConsumerRecord<String, Words> record) {
        System.out.printf("Consumed: by [%s:%s] -> key = %s, value = %s%n", consumerId, consumerGroupId, record.key(), record.value());

        // async commit with callback func
        kafkaSpecificConsumer.commitAsync( (offsets, exception) -> System.out.println("Commit async offset: " + offsets));

        // sync commit
        //        kafkaSpecificConsumer.commitSync();
        //        System.out.println("Commit sync");
    }
}
