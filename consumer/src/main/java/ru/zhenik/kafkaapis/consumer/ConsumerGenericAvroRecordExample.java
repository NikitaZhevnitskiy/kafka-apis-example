package ru.zhenik.kafkaapis.consumer;

import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class ConsumerGenericAvroRecordExample implements Runnable {

    private final String consumerId;
    private final String consumerGroupId;
    private final String topic;
    private final Properties properties;
    private final KafkaConsumer<String, GenericRecord> kafkaGenericConsumer;

    public ConsumerGenericAvroRecordExample(final String topic, final Properties properties) {
        this.topic=topic;
        this.properties = properties;
        // override specific props
        this.properties.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, false);
        this.consumerId=properties.getProperty(ConsumerConfig.CLIENT_ID_CONFIG);
        this.consumerGroupId=properties.getProperty(ConsumerConfig.GROUP_ID_CONFIG);
        this.kafkaGenericConsumer=new KafkaConsumer<String, GenericRecord>(properties);
    }

    @Override
    public void run() {
        kafkaGenericConsumer.subscribe(Collections.singletonList(topic));
        while (true) {
            final ConsumerRecords<String, GenericRecord> records = kafkaGenericConsumer.poll(Duration.ofMillis(100));
            records.iterator().forEachRemaining(record -> {
                System.out.printf("Consumed: by [%s:%s] -> key = %s, value = %s%n", consumerId, consumerGroupId, record.key(), record.value());
            });
        }
    }
}
