package com.interview.testing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Demonstrates test patterns: AAA (Arrange-Act-Assert), test doubles, and AssertJ style.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Test Patterns & Best Practices")
class TestPatternsTest {

    // --- Domain ---

    interface PaymentGateway {
        PaymentResult charge(String cardToken, double amount);
    }

    interface OrderRepository {
        void save(Order order);
    }

    record PaymentResult(boolean success, String transactionId) {}
    record Order(String id, double amount, String status) {}

    static class OrderService {
        private final PaymentGateway paymentGateway;
        private final OrderRepository orderRepo;

        OrderService(PaymentGateway paymentGateway, OrderRepository orderRepo) {
            this.paymentGateway = paymentGateway;
            this.orderRepo = orderRepo;
        }

        Order placeOrder(String orderId, String cardToken, double amount) {
            if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");

            PaymentResult result = paymentGateway.charge(cardToken, amount);
            String status = result.success() ? "CONFIRMED" : "PAYMENT_FAILED";
            Order order = new Order(orderId, amount, status);
            orderRepo.save(order);
            return order;
        }
    }

    // --- Tests using AAA pattern ---

    @Mock PaymentGateway paymentGateway;
    @Mock OrderRepository orderRepo;
    @InjectMocks OrderService orderService;

    @Test
    @DisplayName("AAA: successful order placement")
    void shouldPlaceOrderSuccessfully() {
        // Arrange
        String cardToken = "tok_visa_4242";
        double amount = 99.99;
        when(paymentGateway.charge(cardToken, amount))
                .thenReturn(new PaymentResult(true, "txn_123"));

        // Act
        Order order = orderService.placeOrder("order-1", cardToken, amount);

        // Assert
        assertEquals("CONFIRMED", order.status());
        assertEquals(99.99, order.amount());
        verify(orderRepo).save(any(Order.class));
    }

    @Test
    @DisplayName("AAA: failed payment marks order as failed")
    void shouldMarkOrderAsFailedWhenPaymentDeclined() {
        // Arrange
        when(paymentGateway.charge(anyString(), anyDouble()))
                .thenReturn(new PaymentResult(false, null));

        // Act
        Order order = orderService.placeOrder("order-2", "tok_declined", 50.0);

        // Assert
        assertEquals("PAYMENT_FAILED", order.status());
        verify(orderRepo).save(argThat(o -> o.status().equals("PAYMENT_FAILED")));
    }

    @Test
    @DisplayName("should reject zero amount — guard clause test")
    void shouldRejectZeroAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> orderService.placeOrder("order-3", "tok_visa", 0));

        // Payment should never be attempted
        verifyNoInteractions(paymentGateway);
        verifyNoInteractions(orderRepo);
    }

    @Test
    @DisplayName("should propagate payment gateway exception")
    void shouldPropagateGatewayException() {
        when(paymentGateway.charge(anyString(), anyDouble()))
                .thenThrow(new RuntimeException("Gateway timeout"));

        assertThrows(RuntimeException.class,
                () -> orderService.placeOrder("order-4", "tok_visa", 100.0));

        // Order should NOT be saved if payment throws
        verifyNoInteractions(orderRepo);
    }
}
