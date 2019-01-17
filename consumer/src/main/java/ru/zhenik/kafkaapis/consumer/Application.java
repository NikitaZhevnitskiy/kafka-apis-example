package ru.zhenik.kafkaapis.consumer;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Properties;

public class Application {
    public static void main(String[] args) {
        /**
         * Uncomment to see different examples
         * */
//        execConsumersInOneGroup();
//        execConsumerGeneric();
//        execConsumerExplicitCommit();
    }

    static void execConsumersInOneGroup(){
        // 2 specific consumers in one group
        new Thread(new ConsumerSpecificAvroRecordExample(
                "words-v1",
                consumerConfig("consumer-specific-d", "group-c")))
                .start();

        new Thread(new ConsumerSpecificAvroRecordExample(
                "words-v1",
                consumerConfig("consumer-specific-e", "group-c")))
                .start();
    }
    static void execConsumerGeneric() {
        new Thread(new ConsumerGenericAvroRecordExample(
                "words-v1",
                consumerConfig("consumer-generic-1", "group-2")))
                .start();
    }
    static void execConsumerExplicitCommit() {
        new Thread(new ConsumerExplicitCommit(
                "words-v1",
                consumerConfig("consumer-explicit-commit-a1", "group-3a1"))
        ).start();
    }

    static Properties consumerConfig(final String id, final String groupId) {
        final Properties properties = new Properties();
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, id);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);  //<----------------------
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        properties.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        //Schema registry location.

        return properties;
    }
}
