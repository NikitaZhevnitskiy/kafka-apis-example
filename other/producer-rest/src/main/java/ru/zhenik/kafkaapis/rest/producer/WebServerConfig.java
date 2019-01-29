package ru.zhenik.kafkaapis.rest.producer;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class WebServerConfig {

    public final KafkaConfig kafkaConfig;

    private WebServerConfig(KafkaConfig kafkaConfig) { this.kafkaConfig = kafkaConfig; }

    public static WebServerConfig load() {
        final Config defaultConfig = ConfigFactory.load();
        final String bootstrapServers = defaultConfig.getString("rest-producer.kafka.bootstrap-servers");
        final String schemaUrl = defaultConfig.getString("rest-producer.kafka.schema-registry");
        final String topic = defaultConfig.getString("rest-producer.kafka.topic");
        return new WebServerConfig(new KafkaConfig(bootstrapServers,schemaUrl,topic));
    }


    static class KafkaConfig{
        public final String bootstrapServers;
        public final String schemaRegistry;
        public final String topic;

        public KafkaConfig(String bootstrapServers, String schemaRegistry, String topic) {
            this.bootstrapServers = bootstrapServers;
            this.schemaRegistry = schemaRegistry;
            this.topic = topic;
        }

        @Override
        public String toString() {
            return "KafkaConfig{" +
                    "bootstrapServers='" + bootstrapServers + '\'' +
                    ", schemaRegistry='" + schemaRegistry + '\'' +
                    ", topic='" + topic + '\'' +
                    '}';
        }
    }
}
