package com.interview.testing;

public interface PaymentGateway {
    PaymentResult charge(String cardToken, double amount);
}
