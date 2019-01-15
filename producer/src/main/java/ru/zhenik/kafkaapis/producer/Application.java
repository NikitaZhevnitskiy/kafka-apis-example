package ru.zhenik.kafkaapis.producer;

import ru.zhenik.kafkaapis.schema.avro.Word;
import ru.zhenik.kafkaapis.schema.avro.Words;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class Application {
    public static void main(String[] args) {
        ProducerListWordsExample producer = new ProducerListWordsExample();

        Word word1 = Word.newBuilder().setPayload("cat").build();
        Word word2 = Word.newBuilder().setPayload("home").build();
        Word word3 = Word.newBuilder().setPayload("tac").build();

        Words words = Words.newBuilder().setList(Arrays.asList(word1, word2, word3)).build();

        // async ack
        producer.produceWithAsyncAck(words);

        // sync ack
        try {
            producer.produceWithSyncAck(words);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
