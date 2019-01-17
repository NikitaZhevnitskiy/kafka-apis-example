package ru.zhenik.kafkaapis.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import ru.zhenik.kafkaapis.schema.avro.Words;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;


public class ConsumerSpecificAvroRecordExample implements Runnable {

    private final String consumerId;
    private final String consumerGroupId;
    private final String topic;
    private final Properties properties;
    private final KafkaConsumer<String, Words> kafkaSpecificConsumer;

    public ConsumerSpecificAvroRecordExample(final String topic, final Properties properties) {
        this.topic=topic;
        this.properties = properties;
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
                    final String key = record.key();
                    final Words value = record.value();
                    System.out.printf("Consumed: by [%s:%s] -> key = %s, value = %s%n", consumerId, consumerGroupId, key, value);
                }
            }

        }
}
