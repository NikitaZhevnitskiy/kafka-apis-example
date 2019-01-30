package ru.zhenik.kafkaapis.rest.producer.interfaces.rest;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.pattern.Patterns;
import org.apache.kafka.clients.producer.RecordMetadata;
import ru.zhenik.kafkaapis.rest.producer.interfaces.rest.model.WordsListRepresentation;
import java.time.Duration;
import java.util.concurrent.CompletionStage;

public class WebServerRoutes extends AllDirectives {
    private final ActorRef wordsProducerActor;
    final private LoggingAdapter log;

    public WebServerRoutes(final ActorSystem system, final ActorRef wordsProducerActor) {
        this.log = Logging.getLogger(system, this);
        this.wordsProducerActor = wordsProducerActor;
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
                                    final CompletionStage<Object> recordMetadataCompletionStage =
                                            Patterns.ask(wordsProducerActor, wordsListRepresentation, Duration.ofMillis(500));
//                                                    todo: proper casting
//                                                    .thenApply(metadata -> (RecordMetadata) metadata);
                                    return onSuccess( () -> recordMetadataCompletionStage,
                                            metadata -> complete(StatusCodes.OK, metadata.toString()));

                                })
                        )
                );

        return concat(
                checkRoute, wordsProducerKafkaRoute
        );
    }
}
