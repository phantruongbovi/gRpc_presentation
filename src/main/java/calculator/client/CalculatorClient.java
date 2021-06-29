package calculator.client;

import com.proto.calculator.*;
import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CalculatorClient {
    public static void main(String[] args) throws SSLException {
        CalculatorClient main = new CalculatorClient();
        main.run();
    }

    public void run() throws SSLException {
        //without SSL
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        //with ssl
        ManagedChannel securityChannel = NettyChannelBuilder.forAddress("localhost", 50050)
                .sslContext(GrpcSslContexts.forClient().trustManager(new File("ssl/ca.crt")).build())
                .build();



        doUnary(channel);
        //doStreamingServer(channel);
        //doStreamingClient(channel);
        //doBiDiStreaming(channel);
        //doSquareError(channel);
        //doUnaryTradeWithDeadline(channel);
    }

    //Unary
    private void doUnary(ManagedChannel channel){
        System.out.println("Calling sum!");
        System.out.println("a = 5");
        System.out.println("b = 6");
        CalculateServiceGrpc.CalculateServiceBlockingStub blockingStub = CalculateServiceGrpc.newBlockingStub(channel);
        SumNumberRequest request = SumNumberRequest.newBuilder()
                .setNum1(5)
                .setNum2(6)
                .build();
        SumNumberResponse result = blockingStub.sumNumber(request);
        System.out.println("Kết quả là: " + result.getResult());
    }

    //Streaming server
    private void doStreamingServer(ManagedChannel channel){
        CalculateServiceGrpc.CalculateServiceBlockingStub blockingStub = CalculateServiceGrpc.newBlockingStub(channel);

        DeviceNumberRequest request = DeviceNumberRequest
                .newBuilder()
                .setNum(120)
                .build();

        blockingStub.deviceNumber(request).forEachRemaining(deviceCalculateResponse -> {
            System.out.println(deviceCalculateResponse.getResult());
        });
    }

    //Streaming Client
    private void doStreamingClient(ManagedChannel channel){
        CalculateServiceGrpc.CalculateServiceStub stub = CalculateServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<FindMaxRequest> requestStreamObserver = stub.findMax(new StreamObserver<FindMaxResponse>() {
            @Override
            public void onNext(FindMaxResponse value) {
                System.out.println("Result from server is: " + value.getResult());
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                System.out.println("Completed claim Response!");
                latch.countDown();
            }
        });
        requestStreamObserver.onNext(
                FindMaxRequest.newBuilder().setNum(5).build()
        );
        requestStreamObserver.onNext(
                FindMaxRequest.newBuilder().setNum(7).build()
        );
        requestStreamObserver.onNext(
                FindMaxRequest.newBuilder().setNum(1).build()
        );
        requestStreamObserver.onCompleted();
        try {
            latch.await(3L, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Bi-Di Streaming
    private void doBiDiStreaming(ManagedChannel channel){
        CalculateServiceGrpc.CalculateServiceStub stub = CalculateServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<MedianNumRequest> requestStreamObserver = stub.medianNum(new StreamObserver<MedianNumResponse>() {
            @Override
            public void onNext(MedianNumResponse value) {
                System.out.println("Median is: "+ value.getNum());
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Has completed!");
                latch.countDown();
            }
        });
        Arrays.asList(1, 5, 2, 0, 2, 20).forEach(x -> {
                    System.out.println("Num " + x + " sent!");
                    try {
                        requestStreamObserver.onNext(
                                MedianNumRequest.newBuilder().setNum(x).build()
                        );
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
        );
        requestStreamObserver.onCompleted();
    }

    private void doSquareError(ManagedChannel channel){
        CalculateServiceGrpc.CalculateServiceBlockingStub blockingStub = CalculateServiceGrpc.newBlockingStub(channel);
        try{
            SquareErrorResponse response = blockingStub.squareError(
                    SquareErrorRequest.newBuilder().setNum(-5).build()
            );
            System.out.println("The result is: " + response.getResult());
        } catch (StatusRuntimeException e) {
            System.out.print("The error is ");
            e.printStackTrace();
        }
    }

    private void doUnaryTradeWithDeadline(ManagedChannel channel){
        CalculateServiceGrpc.CalculateServiceBlockingStub blockingStub = CalculateServiceGrpc.newBlockingStub(channel);

        // first call 3000ms deadline
        try {
            TradeWithDeadlineResponse response  = blockingStub.withDeadlineAfter(3000, TimeUnit.MILLISECONDS)
                    .tradeWithDeadline(TradeWithDeadlineRequest.newBuilder().setNum(10).build());
            System.out.println("The time in trading is 3000ms");
            System.out.println("The item has trade is: " + response.getResult());
        }catch (StatusRuntimeException e){
            if(e.getStatus() == Status.DEADLINE_EXCEEDED){
                System.out.println("Deadline has been exceeded, we don't want the response after 3000ms");
            }
            else{
                e.printStackTrace();
            }
        }

        // first call 50ms deadline
        try {
            TradeWithDeadlineResponse response  = blockingStub.withDeadline(Deadline.after(50, TimeUnit.MILLISECONDS))
                    .tradeWithDeadline(TradeWithDeadlineRequest.newBuilder().setNum(10).build());
            System.out.println("The item has trade is: " + response.getResult());
        }catch (StatusRuntimeException e){
            if(e.getStatus() == Status.DEADLINE_EXCEEDED){
                System.out.println("Deadline has been exceeded, we don't want the response after 50ms");
            }
            else{
                e.printStackTrace();
            }
        }

    }
}
