package ru.zhenik.kafkaapis.rest.producer.infrastructure;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import ru.zhenik.kafkaapis.rest.producer.WebServerConfig;
import ru.zhenik.kafkaapis.rest.producer.interfaces.rest.model.WordsListRepresentation;
import ru.zhenik.kafkaapis.rest.producer.interfaces.rest.model.WordsTransformer;
import ru.zhenik.kafkaapis.schema.avro.Words;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;

// https://github.com/akka/akka-http-quickstart-java.g8/tree/10.1.x/src/main/g8/src/main/java/%24package%24
public class WordsProducerActor extends AbstractActor {

    private final LoggingAdapter logger = Logging.getLogger(getContext().getSystem(), this);

    private final Properties properties;
    private final WebServerConfig config;
    private final KafkaProducer<String, Words> producer;
    private final String topicName;
    private final WordsTransformer wordsTransformer;

    public WordsProducerActor(final WebServerConfig config, final WordsTransformer wordsTransformer) {
        this.wordsTransformer = wordsTransformer;
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
    public Future<RecordMetadata> sendToKafka(final Words words) {
        // no key
        final ProducerRecord<String, Words> record = new ProducerRecord<>(topicName, words);
        final Future<RecordMetadata> metadataFuture = producer.send(
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
        return metadataFuture;
    }

    public static Props props(final WebServerConfig config, final WordsTransformer wordsTransformer) {
        return Props.create(WordsProducerActor.class, () -> new WordsProducerActor(config, wordsTransformer));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(WordsListRepresentation.class, wordsListRepresentation -> {
                    final Words wordsAvro = wordsTransformer.parse(wordsListRepresentation);
                    logger.info("Parsed json representation to avro {}", wordsAvro.toString());
                    getSender().tell(Patterns.ask(getSelf(), wordsAvro, Duration.ofMillis(300)), getSelf());
                })
                .match(Words.class, words -> getSender().tell(sendToKafka(words), getSelf()))
                .matchAny(o -> logger.info("received unknown message"))
                .build();
    }


}
