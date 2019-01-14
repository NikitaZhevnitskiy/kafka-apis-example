package ru.zhenik.kafkaapis.producer;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import ru.zhenik.kafkaapis.schema.avro.Words;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class ProducerListWordsExample {
    private final Properties properties;
    private KafkaProducer<String, Words> producer;
    private String topicName;
    private static final Logger logger =
            Logger.getLogger(ProducerListWordsExample.class.getName());

    public ProducerListWordsExample() {
        this.properties=defaultProperties();
        this.topicName="words-v1";
        this.producer=new KafkaProducer<String, Words>(properties);
    }

    private Properties defaultProperties() {
        final Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "producer-words-example-id1");
        // wait for acks from all  brokers when replicated
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        // serializers for key:value pair is [String:Avro]
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        properties.put(ProducerConfig.RETRIES_CONFIG, 0);
        // tune (increase) throughput
        properties.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        properties.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        // be sure to use `http://`
        properties.put("schema.registry.url", "http://localhost:8081");
        return properties;
    }

    // Kafka producer send record async way, but there possibility wait for acks: async or sync (blocking) ways
    public void produceWithAsyncAck(Words words) {
        // no key
        final ProducerRecord<String, Words> record = new ProducerRecord<>(topicName, words);
        producer.send(
                record,
                (metadata, exception) -> {
                    if (exception == null) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("topic", metadata.topic());
                        data.put("partition", metadata.partition());
                        data.put("offset", metadata.offset());
                        data.put("timestamp", metadata.timestamp());
                        logger.info(data.toString());
                    } else {
                        exception.printStackTrace();
                    }
                });
    }

    public void produceWithSyncAck(Words words) throws ExecutionException, InterruptedException {
        // no key
        final ProducerRecord<String, Words> record = new ProducerRecord<>(topicName, words);
        final RecordMetadata recordMetadata = producer.send(record).get();
        logger.info(recordMetadata.toString());
    }

}
