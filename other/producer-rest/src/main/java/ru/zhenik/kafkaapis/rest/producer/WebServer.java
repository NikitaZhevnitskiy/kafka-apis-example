package ru.zhenik.kafkaapis.rest.producer;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Flow;
import ru.zhenik.kafkaapis.rest.producer.infrastructure.WordsProducerActor;
import ru.zhenik.kafkaapis.rest.producer.interfaces.rest.WebServerRoutes;
import ru.zhenik.kafkaapis.rest.producer.interfaces.rest.model.WordsTransformer;

import java.io.IOException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;


public class WebServer {
    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        final ActorSystem system = ActorSystem.create();
        final Materializer materializer = ActorMaterializer.create(system);
        final WebServerConfig serverConfig = WebServerConfig.load();

        final Http http = Http.get(system);

        final ActorRef wordsProducerActor = system.actorOf(WordsProducerActor.props(serverConfig, new WordsTransformer()), "wordsProducerActor");
        final WebServerRoutes webServerRoutes = new WebServerRoutes(system, wordsProducerActor);

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = webServerRoutes.createRoute().flow(system, materializer);
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
