package ru.zhenik.kafkaapis.stream;

import ru.zhenik.kafkaapis.schema.avro.Word;
import ru.zhenik.kafkaapis.schema.avro.Words;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Application {
    public static void main(String[] args) {
        // init
        final StreamProcessing streamProcessing = new StreamProcessing();
        // run
        streamProcessing.run();
        // shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(streamProcessing::stopStreams));
//        final Words words = Words.newBuilder().setList(Collections.singletonList(Word.newBuilder().setPayload("mad").build())).build();
//        System.out.println(words.getList());
//        final List<Word> listWithOneWord = Collections.singletonList(Word.newBuilder().setPayload("cat").build());
//        final List<Word> previousList = words.getList();
//        final List<Word> newList = Stream.concat(listWithOneWord.stream(), previousList.stream()).collect(Collectors.toList());
//        System.out.println(newList);
    }
}
