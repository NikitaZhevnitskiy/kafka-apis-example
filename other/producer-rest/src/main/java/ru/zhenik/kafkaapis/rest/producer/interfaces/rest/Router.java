package ru.zhenik.kafkaapis.rest.producer.interfaces.rest;

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import ru.zhenik.kafkaapis.rest.producer.infrastructure.WordsProducer;
import ru.zhenik.kafkaapis.rest.producer.interfaces.rest.model.WordsListRepresentation;
import ru.zhenik.kafkaapis.rest.producer.interfaces.rest.model.WordsTransformer;

public class Router extends AllDirectives {
    private final WordsTransformer wordsTransformer;
    private final WordsProducer wordsProducer;

    public Router(WordsTransformer wordsTransformer, WordsProducer wordsProducer) {
        this.wordsTransformer = wordsTransformer;
        this.wordsProducer = wordsProducer;
    }

    public Route createRoute() {

        // This handler generates responses to `/hello?name=XXX` requests
        final Route checkRoute =
                path("check", () ->
                        get(() ->
                                complete("Say hello to akka-http")));


        final Route wordsProducerKafkaRoute =
                path("words", () ->
                        post(() -> entity(Jackson.unmarshaller(WordsListRepresentation.class), wordsListRepresentation -> {
                                    System.out.println(wordsListRepresentation);
                                    wordsProducer.sendToKafka(wordsTransformer.parse(wordsListRepresentation));
                                    return complete(StatusCodes.OK, wordsListRepresentation, Jackson.<WordsListRepresentation>marshaller());
                                })
                        )
                );

        return concat(
                checkRoute, wordsProducerKafkaRoute
        );
    }
}
