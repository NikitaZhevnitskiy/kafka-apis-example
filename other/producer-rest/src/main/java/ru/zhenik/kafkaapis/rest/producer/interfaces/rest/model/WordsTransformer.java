package ru.zhenik.kafkaapis.rest.producer.interfaces.rest.model;

import ru.zhenik.kafkaapis.schema.avro.Word;
import ru.zhenik.kafkaapis.schema.avro.Words;

import java.util.stream.Collectors;

public class WordsTransformer {

    public Words parse(final WordsListRepresentation wordsListRepresentation) {
        return Words.newBuilder()
                .setList(
                        wordsListRepresentation
                                .getWords()
                                .stream()
                                .map(line -> Word.newBuilder().setPayload(line).build())
                                .collect(Collectors.toList())
                )
                .build();
    }

    public WordsListRepresentation parse(final Words words) {
        return new WordsListRepresentation(
                words.getList().stream()
                        .map(Word::getPayload)
                        .collect(Collectors.toList())
        );
    }
}
