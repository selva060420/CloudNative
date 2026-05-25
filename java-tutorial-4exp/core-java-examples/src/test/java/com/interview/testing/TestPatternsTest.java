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
 * Demonstrates test patterns: AAA (Arrange-Act-Assert), test doubles, guard clauses.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Test Patterns & Best Practices")
class TestPatternsTest {

    @Mock PaymentGateway paymentGateway;
    @Mock OrderRepository orderRepo;
    @InjectMocks OrderService orderService;

    @Test
    @DisplayName("AAA: successful order placement")
    void shouldPlaceOrderSuccessfully() {
        // Arrange
        when(paymentGateway.charge("tok_visa_4242", 99.99))
                .thenReturn(new PaymentResult(true, "txn_123"));

        // Act
        Order order = orderService.placeOrder("order-1", "tok_visa_4242", 99.99);

        // Assert
        assertEquals("CONFIRMED", order.status());
        assertEquals(99.99, order.amount());
        verify(orderRepo).save(any(Order.class));
    }

    @Test
    @DisplayName("AAA: failed payment marks order as failed")
    void shouldMarkOrderAsFailedWhenPaymentDeclined() {
        when(paymentGateway.charge(anyString(), anyDouble()))
                .thenReturn(new PaymentResult(false, null));

        Order order = orderService.placeOrder("order-2", "tok_declined", 50.0);

        assertEquals("PAYMENT_FAILED", order.status());
        verify(orderRepo).save(argThat(o -> o.status().equals("PAYMENT_FAILED")));
    }

    @Test
    @DisplayName("should reject zero amount — guard clause test")
    void shouldRejectZeroAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> orderService.placeOrder("order-3", "tok_visa", 0));

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

        verifyNoInteractions(orderRepo);
    }
}
