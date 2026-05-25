package com.interview.testing;

public class OrderService {
    private final PaymentGateway paymentGateway;
    private final OrderRepository orderRepo;

    public OrderService(PaymentGateway paymentGateway, OrderRepository orderRepo) {
        this.paymentGateway = paymentGateway;
        this.orderRepo = orderRepo;
    }

    public Order placeOrder(String orderId, String cardToken, double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");

        PaymentResult result = paymentGateway.charge(cardToken, amount);
        String status = result.success() ? "CONFIRMED" : "PAYMENT_FAILED";
        Order order = new Order(orderId, amount, status);
        orderRepo.save(order);
        return order;
    }
}
