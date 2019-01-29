package ru.zhenik.kafkaapis.rest.producer;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.*;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.japi.Function;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;


public class WebServer {
    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        final ActorSystem system = ActorSystem.create();
        final Materializer materializer = ActorMaterializer.create(system);

        final Http http = Http.get(system);
        final Router router = new Router();

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = router.createRoute().flow(system, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(routeFlow,
                ConnectHttp.toHost("localhost", 8080), materializer);
        System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");

        System.in.read(); // let it run until user presses return

        // graceful shutdown
        binding
                .thenCompose(ServerBinding::unbind) // trigger unbinding from the port
                .thenAccept(unbound -> system.terminate()); // and shutdown when done

    }
}
