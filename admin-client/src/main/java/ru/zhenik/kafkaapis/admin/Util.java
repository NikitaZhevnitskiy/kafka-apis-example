package ru.zhenik.kafkaapis.admin;

import org.apache.kafka.clients.admin.AdminClientConfig;

import java.util.Properties;

public class Util {
    public static Properties buildDefaultClientConfig() {
        final Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(AdminClientConfig.CLIENT_ID_CONFIG, "admin-client-id1");
        properties.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 500);
        return properties;
    }
}
