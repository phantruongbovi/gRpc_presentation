package blog.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class BlogServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Hello, this is server Blog server");
        //With out ssl
        Server server = ServerBuilder.forPort(50051)
                .addService(new BlogServerImpl())
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
