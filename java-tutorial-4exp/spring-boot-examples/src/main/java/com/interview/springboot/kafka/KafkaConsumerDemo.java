package com.interview.springboot.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Kafka Consumer — manual commit, error handling, DLQ pattern.
 *
 * Interview points:
 * - Manual ack: process first, commit after (at-least-once)
 * - Consumer group: each partition assigned to one consumer
 * - concurrency = number of threads (match partition count)
 * - DLQ: failed messages go to separate topic after retries
 */
@Service
public class KafkaConsumerDemo {

    /**
     * Basic consumer with manual acknowledgment.
     * If processing fails and we don't ack, message is redelivered.
     */
    @KafkaListener(
        topics = "order-events",
        groupId = "order-processing-group",
        concurrency = "3" // 3 threads — one per partition
    )
    public void handleOrderEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment ack) {

        System.out.printf("Received: partition=%d, offset=%d, key=%s, msg=%s%n",
            partition, offset, key, message);

        try {
            processOrder(message);
            ack.acknowledge(); // commit offset only after successful processing
        } catch (Exception e) {
            System.err.println("Processing failed, will be retried: " + e.getMessage());
            // Don't ack — message will be redelivered (or sent to DLQ after retries)
        }
    }

    /**
     * DLQ consumer — handles messages that failed all retries.
     */
    @KafkaListener(topics = "order-events.DLT", groupId = "dlq-handler")
    public void handleDeadLetter(@Payload String message,
                                  @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        System.err.printf("DLQ: key=%s, msg=%s — requires manual investigation%n", key, message);
        // Alert ops team, store in error DB for manual retry
    }

    private void processOrder(String message) {
        // Simulate processing — throw to test DLQ
        if (message.contains("FAIL")) {
            throw new RuntimeException("Simulated processing failure");
        }
        System.out.println("Order processed successfully: " + message);
    }
}
