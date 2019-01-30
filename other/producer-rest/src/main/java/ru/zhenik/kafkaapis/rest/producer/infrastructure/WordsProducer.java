package ru.zhenik.kafkaapis.rest.producer.infrastructure;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import ru.zhenik.kafkaapis.rest.producer.WebServerConfig;
import ru.zhenik.kafkaapis.schema.avro.Words;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

// todo: make actor
public class WordsProducer {

    private static final Logger logger = Logger.getLogger(WordsProducer.class.getName());
    private final Properties properties;
    private final WebServerConfig config;
    private KafkaProducer<String, Words> producer;
    private String topicName;

    public WordsProducer(final WebServerConfig config) {
        this.config = config;
        this.properties = defaultProperties();
        this.topicName = config.kafkaConfig.topic;
        this.producer = new KafkaProducer<String, Words>(properties);
    }

    private Properties defaultProperties() {
        final Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.kafkaConfig.bootstrapServers);
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "akka-http-rest-words-producer-id1");
        // serializers for key:value pair is [String:Avro]
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        // be sure to use `http://`
        properties.put("schema.registry.url", config.kafkaConfig.schemaRegistry);
        return properties;
    }

    // Kafka producer send record async way, but there possibility wait for acks: async or sync (blocking) ways
    public void sendToKafka(Words words) {
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

//    public void produceWithSyncAck(Words words) throws ExecutionException, InterruptedException {
//        // no key
//        final ProducerRecord<String, Words> record = new ProducerRecord<>(topicName, words);
//        final RecordMetadata recordMetadata = producer.send(record).get();
//        logger.info(recordMetadata.toString());
//    }
}
