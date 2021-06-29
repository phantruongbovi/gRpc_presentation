package calculator.server;

import com.proto.calculator.*;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class CalculatingServiceImpl extends CalculateServiceGrpc.CalculateServiceImplBase {
    //Unary
    @Override
    public void sumNumber(SumNumberRequest request, StreamObserver<SumNumberResponse> responseObserver) {
        System.out.println("Client call sumNumber!");
        int num1 = request.getNum1();
        int num2 = request.getNum2();

        int result = num1 + num2;
        SumNumberResponse response = SumNumberResponse
                .newBuilder()
                .setResult(result)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    //Streaming Server
    @Override
    public void deviceNumber(DeviceNumberRequest request, StreamObserver<DeviceNumberResponse> responseObserver) {
        System.out.println("Client call deviceNumber!");
        int num = request.getNum();
        try{
            int k = 2;
            while (num > 1){
                if(num%k==0){
                    num/=k;
                    DeviceNumberResponse response = DeviceNumberResponse.newBuilder().setResult(k).build();
                    responseObserver.onNext(response);
                    Thread.sleep(500L);
                }
                else{
                    k+=1;
                }
            }
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
        finally {
            responseObserver.onCompleted();
        }
    }

    //Streaming Client
    @Override
    public StreamObserver<FindMaxRequest> findMax(StreamObserver<FindMaxResponse> responseObserver) {
        System.out.println("Client call findMax!");
        StreamObserver<FindMaxRequest> requestStreamObserver = new StreamObserver<FindMaxRequest>() {
            int max;
            int firstNum = 0;
            @Override
            public void onNext(FindMaxRequest value) {
                if(firstNum == 0){
                    max = value.getNum();
                    firstNum+=1;
                }
                else{
                    if(max < value.getNum()){
                        max = value.getNum();
                    }
                }
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                FindMaxResponse response = FindMaxResponse.newBuilder().setResult(max).build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
        return requestStreamObserver;
    }

    //Bi-Di Streaming
    @Override
    public StreamObserver<MedianNumRequest> medianNum(StreamObserver<MedianNumResponse> responseObserver) {
        System.out.println("Client call medianNum!");
        StreamObserver<MedianNumRequest> requestStreamObserver = new StreamObserver<MedianNumRequest>() {
            int sum = 0;
            int count = 0;
            @Override
            public void onNext(MedianNumRequest value) {
                sum+=value.getNum();
                count+=1;
                responseObserver.onNext(
                        MedianNumResponse.newBuilder().setNum(sum/count).build()
                );
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
        return requestStreamObserver;
    }

    //Catch Error
    @Override
    public void squareError(SquareErrorRequest request, StreamObserver<SquareErrorResponse> responseObserver) {
        System.out.println("Client call squareError!");
        int num = request.getNum();
        if(num >= 0){
            double result = Math.sqrt(num);
            responseObserver.onNext(
                    SquareErrorResponse.newBuilder().setResult(result).build()
            );
            responseObserver.onCompleted();
        }
        else{
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription("The number being sent is not a positive")
                            .augmentDescription("The number is: " + num)
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void tradeWithDeadline(TradeWithDeadlineRequest request, StreamObserver<TradeWithDeadlineResponse> responseObserver) {
        System.out.println("Client call tradeWithDeadline!");
        Context current = Context.current();
        try {
            for(int i =0;i <3;i++) {
                if(!current.isCancelled()) {
                    Thread.sleep(100);
                }
                else{
                    return;
                }
            }
            int num = request.getNum();
            responseObserver.onNext(
                    TradeWithDeadlineResponse.newBuilder().setResult(num*2).build()
            );
            System.out.println("Successfully trading item " + num + " !");
            responseObserver.onCompleted();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
