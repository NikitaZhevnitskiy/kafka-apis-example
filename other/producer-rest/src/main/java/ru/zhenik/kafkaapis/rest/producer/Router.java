package ru.zhenik.kafkaapis.rest.producer;

import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.marshallers.jackson.Jackson;

public class Router extends AllDirectives {

    public Route createRoute() {

        // This handler generates responses to `/hello?name=XXX` requests
        final Route checkRoute =
                path("check", () ->
                        get(() ->
                                complete("Say hello to akka-http")));

        final Route producerKafkaRoute =
                path("pets", () ->
                        post(() -> entity(Jackson.unmarshaller(Pet.class), thePet -> {
                                    System.out.println(thePet);
                                    return complete(StatusCodes.OK, thePet, Jackson.<Pet>marshaller());
                                })
                        )
                );

        return concat(
                checkRoute, producerKafkaRoute
        );
    }
}
