package calculator.server;


import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;

import java.io.File;
import java.io.IOException;

public class CalculatorServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Hello, this is server Calculator");
        //With out ssl
        Server server = ServerBuilder.forPort(50051)
                .addService(new CalculatingServiceImpl())
                .addService((ProtoReflectionService.newInstance()))
                .build();


        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Request shutdown server!");
            server.shutdown();
            System.out.println("Successfully stopped server");
        }));
        server.awaitTermination();
    }
}
