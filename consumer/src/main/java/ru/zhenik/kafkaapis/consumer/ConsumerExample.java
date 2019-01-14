package ru.zhenik.kafkaapis.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import ru.zhenik.kafkaapis.schema.avro.Words;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class ConsumerExample implements Runnable {

    private final String consumerId;
    private final String consumerGroupId;
    private final String topic;
    private final Properties properties;
    private final KafkaConsumer<String, Words> kafkaSpecificConsumer;

    public ConsumerExample(final String topic, final Properties properties) {
        this.topic=topic;
        this.properties = properties;
        this.consumerId=properties.getProperty(ConsumerConfig.CLIENT_ID_CONFIG);
        this.consumerGroupId=properties.getProperty(ConsumerConfig.GROUP_ID_CONFIG);
        this.kafkaSpecificConsumer=new KafkaConsumer<String, Words>(properties);
    }

    @Override
    public void run() {
        kafkaSpecificConsumer.subscribe(Collections.singletonList(topic));
        while (true) {
            final ConsumerRecords<String, Words> records = kafkaSpecificConsumer.poll(Duration.ofMillis(100));
            records.iterator().forEachRemaining(record ->
                    System.out.println(
                            String.format("[%s:%s] record consumed[%s:%s]", this.consumerId, this.consumerGroupId, record.key(), record.value())));
        }
    }
}
