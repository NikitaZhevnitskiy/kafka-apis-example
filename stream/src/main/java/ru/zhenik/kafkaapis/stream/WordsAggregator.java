package ru.zhenik.kafkaapis.stream;

import ru.zhenik.kafkaapis.schema.avro.Word;
import ru.zhenik.kafkaapis.schema.avro.Words;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WordsAggregator {
    final List<Word> words = new ArrayList<>();


    public WordsAggregator() {
    }

    public WordsAggregator(WordsAggregator wordsAgg1, WordsAggregator wordsAgg2) {
        words.addAll(wordsAgg1.words);
        words.addAll(wordsAgg2.words);
    }

    public WordsAggregator add(Word word) {
        words.add(word);
        return this;
    }


    public byte[] asByteArray() throws IOException {
        return Words.newBuilder().setList(words).build().toByteBuffer().array();
    }

//    public String groupedLimitedBy(Integer limitSize) {
//        Map<String, Long> counted = logs.stream()
//                .map(logEntry -> logEntry.asJsonString())
//                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
//
//        ArrayList<LogEntry> listSubset = new ArrayList<>();
//
//        counted.entrySet().stream()
//                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
//                .limit(limitSize)
//                .forEachOrdered(e -> {
//                    LogEntry logEntry = LogEntry.fromJson(e.getKey(), e.getValue());
//                    listSubset.add(logEntry);
//                });
//
//        return gson.toJson(listSubset);
//    }


}
