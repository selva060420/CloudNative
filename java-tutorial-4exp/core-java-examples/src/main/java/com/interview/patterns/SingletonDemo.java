package com.interview.patterns;

/**
 * All 5 Singleton approaches — thread-safety, lazy loading, serialization safety.
 */
public class SingletonDemo {

    // 1. Eager Initialization
    static class EagerSingleton {
        private static final EagerSingleton INSTANCE = new EagerSingleton();
        private EagerSingleton() {}
        public static EagerSingleton getInstance() { return INSTANCE; }
    }

    // 2. Synchronized Method (slow)
    static class SyncSingleton {
        private static SyncSingleton instance;
        private SyncSingleton() {}
        public static synchronized SyncSingleton getInstance() {
            if (instance == null) instance = new SyncSingleton();
            return instance;
        }
    }

    // 3. Double-Checked Locking (volatile required)
    static class DCLSingleton {
        private static volatile DCLSingleton instance;
        private DCLSingleton() {}
        public static DCLSingleton getInstance() {
            if (instance == null) {
                synchronized (DCLSingleton.class) {
                    if (instance == null) instance = new DCLSingleton();
                }
            }
            return instance;
        }
    }

    // 4. Enum Singleton (Joshua Bloch — reflection & serialization safe)
    enum EnumSingleton {
        INSTANCE;
        public void doWork() { System.out.println("Enum singleton working"); }
    }

    // 5. Bill Pugh (static inner class — lazy + no synchronization)
    static class BillPughSingleton {
        private BillPughSingleton() {}
        private static class Holder {
            private static final BillPughSingleton INSTANCE = new BillPughSingleton();
        }
        public static BillPughSingleton getInstance() { return Holder.INSTANCE; }
    }

    public static void main(String[] args) {
        System.out.println("=== Singleton Approaches ===\n");

        // All return same instance
        System.out.println("Eager: " + (EagerSingleton.getInstance() == EagerSingleton.getInstance()));
        System.out.println("Sync:  " + (SyncSingleton.getInstance() == SyncSingleton.getInstance()));
        System.out.println("DCL:   " + (DCLSingleton.getInstance() == DCLSingleton.getInstance()));
        System.out.println("Enum:  " + (EnumSingleton.INSTANCE == EnumSingleton.INSTANCE));
        System.out.println("Pugh:  " + (BillPughSingleton.getInstance() == BillPughSingleton.getInstance()));

        EnumSingleton.INSTANCE.doWork();
    }
}
