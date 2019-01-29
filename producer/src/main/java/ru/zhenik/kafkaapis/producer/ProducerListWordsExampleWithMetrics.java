package ru.zhenik.kafkaapis.producer;


import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.serialization.StringSerializer;
import ru.zhenik.kafkaapis.schema.avro.Words;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ProducerListWordsExampleWithMetrics implements Runnable {
    private final Properties properties;
    private KafkaProducer<String, Words> producer;
    private String topicName;
    private static final Logger logger =
            Logger.getLogger(ProducerListWordsExampleWithMetrics.class.getName());
    private final Set<String> metricsNameFilter = new HashSet(Arrays.asList(
            "record-queue-time-avg", "record-send-rate", "records-per-request-avg",
            "request-size-max", "network-io-rate", "record-queue-time-avg",
            "incoming-byte-rate", "batch-size-avg", "response-rate", "requests-in-flight"
    ));

    public ProducerListWordsExampleWithMetrics() {
        this.properties=defaultProperties();
        this.topicName="words-v1";
        this.producer=new KafkaProducer<String, Words>(properties);
    }

    private Properties defaultProperties() {
        final Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "producer-words-example-id1");
        // wait for acks from all brokers, when replicated [-1, 0, 1]
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        // serializers for key:value pair is [String:Avro]
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
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

    @Override
    public void run() {
        while (true) {
            final Map<MetricName, ? extends Metric> metrics
                    = producer.metrics();

            displayMetrics(metrics);
            try {
                Thread.sleep(3_000);
            } catch (InterruptedException e) {
                logger.warning("metrics interrupted");
                Thread.interrupted();
                break;
            }
        }
    }
    static class MetricPair {
        private final MetricName metricName;
        private final Metric metric;
        MetricPair(MetricName metricName, Metric metric) {
            this.metricName = metricName;
            this.metric = metric;
        }
        public String toString() {
            return metricName.group() + "." + metricName.name();
        }
    }

    private void displayMetrics(Map<MetricName, ? extends Metric> metrics) {
        final Map<String, MetricPair> metricsDisplayMap = metrics.entrySet().stream()
                //Filter out metrics not in metricsNameFilter
                .filter(metricNameEntry ->
                        metricsNameFilter.contains(metricNameEntry.getKey().name()))
                //Filter out metrics not in metricsNameFilter
                .filter(metricNameEntry ->
                        !Double.isInfinite(metricNameEntry.getValue().value()) &&
                                !Double.isNaN(metricNameEntry.getValue().value()) &&
                                metricNameEntry.getValue().value() != 0
                )
                //Turn Map<MetricName,Metric> into TreeMap<String, MetricPair>
                .map(entry -> new MetricPair(entry.getKey(), entry.getValue()))
                .collect(Collectors.toMap(
                        MetricPair::toString, it -> it, (a, b) -> a, TreeMap::new
                ));


        //Output metrics
        final StringBuilder builder = new StringBuilder(255);
        builder.append("\n---------------------------------------\n");
        metricsDisplayMap.forEach((name, metricPair) -> builder.append(String.format(Locale.US, "%50s%25s\t\t%,-10.2f\t\t%s\n",
                name,
                metricPair.metricName.name(),
                metricPair.metric.metricValue(),
                metricPair.metricName.description()
        )));
        builder.append("\n---------------------------------------\n");
        logger.info(builder.toString());
    }
}
