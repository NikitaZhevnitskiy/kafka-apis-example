package ru.zhenik.kafkaapis.stream;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import ru.zhenik.kafkaapis.schema.avro.Word;
import ru.zhenik.kafkaapis.schema.avro.Words;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamProcessing implements Runnable {
    // final variables
    private static final Logger LOGGER =
            Logger.getLogger(StreamProcessing.class.getName());

    private final KafkaStreams kafkaStreams;
    private final String SCHEMA_URL = "http://localhost:8081";

    public StreamProcessing() {
        this.kafkaStreams = new KafkaStreams(buildTopology(), getProperties());
    }


    private Properties getProperties() {
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "app-steam-processing-id-v1");
        properties.put(StreamsConfig.CLIENT_ID_CONFIG, "steam-processing-id-v1");
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_URL);
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);

//         todo: check what are dir state & specific reader
//        properties.put(StreamsConfig.STATE_DIR_CONFIG, stateDir);
//        properties.put("specific.avro.reader", "true");

        return properties;
    }

    private Topology buildTopology() {
        final StreamsBuilder builder = new StreamsBuilder();
        final Map<String, String> schema = Collections
                .singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, SCHEMA_URL);

        final SpecificAvroSerde<Words> wordsListSerdes = new SpecificAvroSerde<>();
        wordsListSerdes.configure(schema,false);

        final SpecificAvroSerde<Word> wordSerdes = new SpecificAvroSerde<>();
        wordSerdes.configure(schema, false);

        final KStream<String, Words> sourceStream = builder.stream("words-v1", Consumed.with(Serdes.String(), wordsListSerdes));

        sourceStream
                // extract
                .flatMapValues(Words::getList)
                .through("word-v1")
                //
                .map( (ignored, word) -> {
                    final String payload = word.getPayload();
                    final String key = Stream.of(payload.split(""))
                            .sorted()
                            .collect(Collectors.joining());
                    return KeyValue.pair(key, word);
                })
                .to("sortedhash-word-v1");

        return builder.build();
    }



    @Override
    public void run() {
        kafkaStreams.cleanUp();
        kafkaStreams.start();
    }
}
