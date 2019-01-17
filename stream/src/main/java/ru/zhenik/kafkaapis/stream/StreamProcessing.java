package ru.zhenik.kafkaapis.stream;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import ru.zhenik.kafkaapis.schema.avro.Word;
import ru.zhenik.kafkaapis.schema.avro.Words;

import java.util.*;
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

        // !NB source topics should exist before stream topology start
        final KStream<String, Words> sourceStream = builder.stream(Topic.WORDS, Consumed.with(Serdes.String(), wordsListSerdes));

        // extract word from list and add sorted hash as a key
        sourceStream
                // extract
                .flatMapValues(Words::getList)
                .map( (ignored, word) -> {
                    final String payload = word.getPayload();
                    final String key = Stream.of(payload.split(""))
                            .sorted()
                            .collect(Collectors.joining());
                    final Word wordWithHash = Word.newBuilder().setPayload(payload).setSorted(key).build();

                    final KeyValue<String, Word> pair = KeyValue.pair(key, wordWithHash);
                    System.out.println("PAIR: "+pair);
                    return pair;
                })
                .to(Topic.WORD, Produced.with(Serdes.String(), wordSerdes));

        // !NB source topics should exist before stream topology start
        final KStream<String, Word> wordStream = builder.stream(Topic.WORD, Consumed.with(Serdes.String(), wordSerdes));

        // [key:value] => [hash:anagramCount]
        wordStream
                .groupByKey()
                .count()
                .toStream()
                .to(Topic.ANAGRAM_HASH_COUNT, Produced.with( Serdes.String(), Serdes.Long() ));

        // [key:value] => [hash:wordsList]
        wordStream
                .groupByKey()
                .aggregate(
                        () -> Words.newBuilder().setList(Collections.emptyList()).build(),
                        (aggKey, newValue, aggValue) -> {
                            System.out.println("OLD_LIST: "+aggValue.getList());
                            final List<Word> listWithOneWord = Collections.singletonList(newValue);
                            final List<Word> previousList = aggValue.getList();
                            final List<Word> newList = Stream.concat(listWithOneWord.stream(), previousList.stream()).collect(Collectors.toList());
                            System.out.println("NEW_LIST: "+newList);
                            return Words.newBuilder().setList(newList).build();
                        }
                )
                .toStream()
                .to(Topic.HASH_ANAGRAMS, Produced.with( Serdes.String(), wordsListSerdes) );


        final Topology topology = builder.build();
        System.out.println(topology.describe());
        return topology;
    }

    public void stopStreams() { Optional.ofNullable(kafkaStreams).ifPresent(KafkaStreams::close); }


    @Override
    public void run() {
        kafkaStreams.cleanUp();
        kafkaStreams.start();
    }
}
