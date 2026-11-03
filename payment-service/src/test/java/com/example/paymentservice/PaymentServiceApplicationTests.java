package com.example.paymentservice;

import com.example.paymentservice.grpc.PaymentRequest;
import com.example.paymentservice.grpc.PaymentResponse;
import com.example.paymentservice.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "grpc.server.port=0")
class PaymentServiceApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void processesValidPayment() {
        PaymentService service = new PaymentService();
        PaymentResponseObserver observer = new PaymentResponseObserver();

        service.processPayment(PaymentRequest.newBuilder()
                .setOrderId("42")
                .setAmount(10)
                .setCurrency("USD")
                .build(), observer);

        assertTrue(observer.response.getSuccess());
        assertTrue(observer.response.getTransactionId().startsWith("txn_"));
    }

    @Test
    void rejectsInvalidPayment() {
        PaymentService service = new PaymentService();
        PaymentResponseObserver observer = new PaymentResponseObserver();

        service.processPayment(PaymentRequest.newBuilder()
                .setOrderId("42")
                .setAmount(0)
                .setCurrency("USD")
                .build(), observer);

        assertFalse(observer.response.getSuccess());
        assertEquals("A valid orderId, positive amount, and currency are required",
                observer.response.getErrorMessage());
    }

    private static final class PaymentResponseObserver
            implements io.grpc.stub.StreamObserver<PaymentResponse> {
        private PaymentResponse response;

        @Override
        public void onNext(PaymentResponse response) {
            this.response = response;
        }

        @Override
        public void onError(Throwable throwable) {
            throw new AssertionError(throwable);
        }

        @Override
        public void onCompleted() {
        }
    }
}
