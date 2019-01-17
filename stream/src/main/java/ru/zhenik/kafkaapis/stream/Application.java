package ru.zhenik.kafkaapis.stream;

import ru.zhenik.kafkaapis.admin.CreateTopics;

public class Application {
    public static void main(String[] args) {
        // init
            // source topics should exist before start stream topology
        CreateTopics.createTopic(Topic.WORDS, 5, (short)1);
        CreateTopics.createTopic(Topic.WORD, 5, (short)1);
        final StreamProcessing streamProcessing = new StreamProcessing();

        // run
        streamProcessing.run();

        // shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(streamProcessing::stopStreams));
    }
}
