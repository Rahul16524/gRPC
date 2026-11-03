package com.example.orderservice;

import com.example.orderservice.client.PaymentClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentClient paymentClient;

    @Test
    void contextLoads() {
    }

    @Test
    void createsOrderAndProcessesPayment() throws Exception {
        when(paymentClient.processPayment(anyString(), anyDouble(), eq("USD")))
                .thenReturn("txn_test");

        MvcResult createResult = mockMvc.perform(post("/api/order")
                        .contentType("application/json")
                        .content("""
                                {
                                  "customerId": 42,
                                  "items": [
                                    {
                                      "productId": 123,
                                      "quantity": 2,
                                      "unitPrice": 4.5
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalPrice").value(9.0))
                .andExpect(jsonPath("$.items[0].totalAmount").value(9.0))
                .andReturn();

        JsonNode createdOrder = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long orderId = createdOrder.get("id").asLong();

        mockMvc.perform(post("/api/order/payment/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("txn_test"))
                .andExpect(jsonPath("$.totalPrice").value(9.0));

        verify(paymentClient).processPayment(Long.toString(orderId), 9.0, "USD");
    }

    @Test
    void createsOrderWithTrailingSlash() throws Exception {
        mockMvc.perform(post("/api/order/")
                        .contentType("application/json")
                        .content("""
                                {
                                  "customerId": 12,
                                  "items": [
                                    {
                                      "productId": 999,
                                      "quantity": 9,
                                      "unitPrice": 8
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalPrice").value(72.0));
    }

    @Test
    void rejectsOrderWithoutItems() throws Exception {
        mockMvc.perform(post("/api/order")
                        .contentType("application/json")
                        .content("""
                                {"customerId": 12, "items": []}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsOrderWithInvalidItem() throws Exception {
        mockMvc.perform(post("/api/order")
                        .contentType("application/json")
                        .content("""
                                {
                                  "customerId": 12,
                                  "items": [{"productId": 999, "quantity": 0, "unitPrice": 8}]
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsOrderWithMissingCustomerId() throws Exception {
        mockMvc.perform(post("/api/order")
                        .contentType("application/json")
                        .content("""
                                {
                                  "items": [{"productId": 999, "quantity": 1, "unitPrice": 8}]
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createsStandaloneOrderItem() throws Exception {
        mockMvc.perform(post("/api/order-item")
                        .contentType("application/json")
                        .content("""
                                {
                                  "productId": 999,
                                  "quantity": 9,
                                  "unitPrice": 8,
                                  "totalAmount": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount").value(72.0));
    }

    @Test
    void rejectsInvalidStandaloneOrderItem() throws Exception {
        mockMvc.perform(post("/api/order-item")
                        .contentType("application/json")
                        .content("""
                                {"productId": 999, "quantity": 0, "unitPrice": 8}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsStandaloneOrderItemWithOverflowingTotal() throws Exception {
        mockMvc.perform(post("/api/order-item")
                        .contentType("application/json")
                        .content("""
                                {"productId": 999, "quantity": 2, "unitPrice": 1.7E308}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createsStandaloneOrderItemsInBulk() throws Exception {
        mockMvc.perform(post("/api/order-item/list")
                        .contentType("application/json")
                        .content("""
                                [
                                  {"productId": 999, "quantity": 9, "unitPrice": 8},
                                  {"productId": 1000, "quantity": 2, "unitPrice": 3.5}
                                ]
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].totalAmount").value(72.0))
                .andExpect(jsonPath("$[1].totalAmount").value(7.0));
    }

    @Test
    void rejectsInvalidItemInBulkRequest() throws Exception {
        mockMvc.perform(post("/api/order-item/list")
                        .contentType("application/json")
                        .content("""
                                [
                                  {"productId": 999, "quantity": 9, "unitPrice": 8},
                                  {"productId": 1000, "quantity": 0, "unitPrice": 3.5}
                                ]
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsEmptyBulkRequest() throws Exception {
        mockMvc.perform(post("/api/order-item/list")
                        .contentType("application/json")
                        .content("[]"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsNotFoundWhenPayingForMissingOrder() throws Exception {
        mockMvc.perform(post("/api/order/payment/987654321"))
                .andExpect(status().isNotFound());
    }

    @Test
    void reportsUnavailablePaymentService() throws Exception {
        when(paymentClient.processPayment(anyString(), anyDouble(), eq("USD")))
                .thenThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE));
        MvcResult result = mockMvc.perform(post("/api/order")
                        .contentType("application/json")
                        .content("""
                                {
                                  "customerId": 42,
                                  "items": [{"productId": 123, "quantity": 2, "unitPrice": 4.5}]
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        long orderId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(post("/api/order/payment/{orderId}", orderId))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void rejectsUnsupportedHttpMethod() throws Exception {
        mockMvc.perform(get("/api/order"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void rejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/api/order-item")
                        .contentType("application/json")
                        .content("{invalid json"))
                .andExpect(status().isBadRequest());
    }
}
