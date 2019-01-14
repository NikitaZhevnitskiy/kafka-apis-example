package ru.zhenik.kafkaapis.consumer;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Properties;

public class Application {
    public static void main(String[] args) {
        new Thread(new ConsumerExample("words-v1", consumerConfig("consumer-1", "group-1"))).start();
    }

    static Properties consumerConfig(final String id, final String groupId) {
        final Properties properties = new Properties();
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, id);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());  //<----------------------
        //Use Specific Record or else you get Avro GenericRecord.
        properties.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");
        //Schema registry location.
        properties.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        return properties;
    }
}
