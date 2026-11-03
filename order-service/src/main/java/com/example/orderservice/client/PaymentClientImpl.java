package com.example.orderservice.client;

import com.example.paymentservice.grpc.PaymentRequest;
import com.example.paymentservice.grpc.PaymentResponse;
import com.example.paymentservice.grpc.PaymentServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.TimeUnit;

@Service
public class PaymentClientImpl implements PaymentClient {
    private final ManagedChannel channel;
    private final PaymentServiceGrpc.PaymentServiceBlockingStub paymentServiceStub;

    public PaymentClientImpl(@Value("${payment.service.address}") String paymentServiceAddress) {
        channel = ManagedChannelBuilder.forTarget(paymentServiceAddress)
                .usePlaintext() // In production, you'd use SSL/TLS
                .build();
        paymentServiceStub = PaymentServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public String processPayment(String orderId, double amount, String currency) {
        PaymentRequest request = PaymentRequest.newBuilder()
                .setOrderId(orderId)
                .setAmount(amount)
                .setCurrency(currency)
                .build();

        try {
            PaymentResponse response = paymentServiceStub
                    .withDeadlineAfter(5, TimeUnit.SECONDS)
                    .processPayment(request);
            if (!response.getSuccess()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, response.getErrorMessage());
            }
            return response.getTransactionId();
        } catch (StatusRuntimeException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Payment service is unavailable", exception);
        }
    }

    @PreDestroy
    public void closeChannel() {
        channel.shutdown();
    }
}
