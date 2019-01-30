package ru.zhenik.kafkaapis.rest.producer.interfaces.rest.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class WordsListRepresentation {
    private final List<String> words;

    @JsonCreator
    public WordsListRepresentation(@JsonProperty("words") List<String> words) {
        this.words = words;
    }

    public List<String> getWords() {
        return words;
    }

    @Override
    public String toString() {
        return "WordsListRepresentation{" +
                "words=" + words +
                '}';
    }
}
