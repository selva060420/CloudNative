package com.interview.corejava.serialization;

import java.io.*;
import java.util.Map;

/**
 * Demonstrates Serialization: serialVersionUID, transient fields, and versioning issues.
 * Context: Distributed systems where objects are sent over network (Kafka, caching).
 */
public class SerializationDemo {

    static class UserSession implements Serializable {
        private static final long serialVersionUID = 1L; // Explicit — safe across deploys
        private final String sessionId;
        private final String userId;
        private transient String authToken; // NOT serialized (sensitive)

        UserSession(String sessionId, String userId, String authToken) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.authToken = authToken;
        }

        @Override
        public String toString() {
            return "Session{id=" + sessionId + ", user=" + userId + ", token=" + authToken + "}";
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Serialization Demo ===");
        serializeDeserialize();

        System.out.println("\n=== Transient Field Behavior ===");
        transientDemo();
    }

    static void serializeDeserialize() throws Exception {
        UserSession session = new UserSession("sess-001", "user-123", "secret-token");
        System.out.println("Before: " + session);

        // Serialize to bytes (simulates sending over Kafka / storing in cache)
        byte[] bytes;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(session);
            bytes = bos.toByteArray();
        }
        System.out.println("Serialized size: " + bytes.length + " bytes");

        // Deserialize (simulates receiving in another microservice)
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            UserSession restored = (UserSession) ois.readObject();
            System.out.println("After:  " + restored);
        }
    }

    static void transientDemo() {
        UserSession session = new UserSession("sess-002", "user-456", "my-secret");
        System.out.println("Original token: " + session.authToken);

        try {
            byte[] bytes;
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(session);
                bytes = bos.toByteArray();
            }
            try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                 ObjectInputStream ois = new ObjectInputStream(bis)) {
                UserSession restored = (UserSession) ois.readObject();
                System.out.println("Restored token (transient → null): " + restored.authToken);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
