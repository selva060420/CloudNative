package com.interview.springboot.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka Producer — key-based partitioning, async send with callbacks.
 *
 * Interview points:
 * - Key determines partition (ordering per key guaranteed)
 * - Async send returns CompletableFuture for non-blocking
 * - acks=all ensures durability (configured in application.properties)
 */
@Service
public class KafkaProducerDemo {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerDemo(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Send with key — all messages with same key go to same partition (ordering).
     */
    public CompletableFuture<SendResult<String, String>> sendOrderEvent(String orderId, String event) {
        return kafkaTemplate.send("order-events", orderId, event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    System.err.println("Failed to send: " + ex.getMessage());
                } else {
                    System.out.printf("Sent to partition=%d, offset=%d, key=%s%n",
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        orderId);
                }
            });
    }

    /**
     * Fire-and-forget (no callback) — highest throughput, risk of silent data loss.
     */
    public void sendFireAndForget(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }
}
