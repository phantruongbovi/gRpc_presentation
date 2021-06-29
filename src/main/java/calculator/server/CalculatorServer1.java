package calculator.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.File;
import java.io.IOException;

public class CalculatorServer1 {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Hello, this is server with ssl Calculator");
        //with ssl
        Server server = ServerBuilder.forPort(50050)
                .addService(new CalculatingServiceImpl())
                .useTransportSecurity(
                        new File("ssl/server.crt"),
                        new File("ssl/server.pem")
                )
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
