package com.example.paymentservice.service;

import com.example.paymentservice.grpc.PaymentRequest;
import com.example.paymentservice.grpc.PaymentResponse;
import com.example.paymentservice.grpc.PaymentServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class PaymentService extends PaymentServiceGrpc.PaymentServiceImplBase {

    @Override
    public void processPayment(PaymentRequest request, StreamObserver<PaymentResponse> responseObserver) {
        if (request.getOrderId().isBlank() || !Double.isFinite(request.getAmount())
                || request.getAmount() <= 0 || request.getCurrency().isBlank()) {
            responseObserver.onNext(PaymentResponse.newBuilder()
                    .setSuccess(false)
                    .setErrorMessage("A valid orderId, positive amount, and currency are required")
                    .build());
            responseObserver.onCompleted();
            return;
        }

        PaymentResponse response = PaymentResponse.newBuilder()
                .setSuccess(true)
                .setTransactionId("txn_" + System.currentTimeMillis())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
