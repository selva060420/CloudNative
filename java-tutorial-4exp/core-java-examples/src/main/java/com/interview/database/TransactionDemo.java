package com.interview.database;

import java.sql.*;

/**
 * Demonstrates ACID transaction management in JDBC.
 *
 * Key concepts:
 * - Auto-commit vs manual transactions
 * - Commit and rollback
 * - Isolation levels (READ_COMMITTED, REPEATABLE_READ, SERIALIZABLE)
 * - Savepoints for partial rollback
 * - Deadlock detection and handling
 *
 * NOTE: Uses H2 in-memory DB — no external setup needed.
 */
public class TransactionDemo {

    private static final String URL = "jdbc:h2:mem:txdb;DB_CLOSE_DELAY=-1";

    public static void main(String[] args) throws Exception {
        System.out.println("=== Transaction Management Demo ===\n");

        setupSchema();
        insertAccount("Alice", 1000.0);
        insertAccount("Bob", 500.0);

        printBalances("Initial state");

        // Demo 1: Successful transfer
        System.out.println("\n--- Demo 1: Successful Transfer (Alice → Bob: $200) ---");
        transfer("Alice", "Bob", 200.0);
        printBalances("After transfer");

        // Demo 2: Failed transfer (rollback)
        System.out.println("\n--- Demo 2: Failed Transfer with Rollback ---");
        transferWithFailure("Alice", "Bob", 5000.0); // Insufficient funds
        printBalances("After failed transfer (unchanged)");

        // Demo 3: Savepoints
        System.out.println("\n--- Demo 3: Savepoints (Partial Rollback) ---");
        savepointDemo();
        printBalances("After savepoint demo");

        // Demo 4: Isolation levels
        System.out.println("\n--- Demo 4: Isolation Levels ---");
        isolationLevelDemo();
    }

    /** Successful money transfer — both debit and credit in one transaction */
    private static void transfer(String from, String to, double amount) throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, "sa", "")) {
            conn.setAutoCommit(false); // START TRANSACTION
            try {
                // Debit
                updateBalance(conn, from, -amount);
                // Credit
                updateBalance(conn, to, amount);

                conn.commit(); // Both succeed → commit
                System.out.println("  Transfer committed: $" + amount + " from " + from + " to " + to);
            } catch (SQLException e) {
                conn.rollback(); // Any failure → undo everything
                System.out.println("  Transfer rolled back: " + e.getMessage());
            }
        }
    }

    /** Transfer that fails — demonstrates rollback */
    private static void transferWithFailure(String from, String to, double amount) throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, "sa", "")) {
            conn.setAutoCommit(false);
            try {
                // Check balance first
                double balance = getBalance(conn, from);
                if (balance < amount) {
                    throw new SQLException("Insufficient funds: " + from +
                            " has $" + balance + ", needs $" + amount);
                }
                updateBalance(conn, from, -amount);
                updateBalance(conn, to, amount);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback(); // Rollback — no money lost!
                System.out.println("  Rolled back: " + e.getMessage());
            }
        }
    }

    /** Savepoints allow partial rollback within a transaction */
    private static void savepointDemo() throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, "sa", "")) {
            conn.setAutoCommit(false);
            try {
                // Step 1: Give Alice a bonus (keep this)
                updateBalance(conn, "Alice", 100.0);
                Savepoint sp = conn.setSavepoint("after_bonus");
                System.out.println("  Savepoint set after Alice bonus (+$100)");

                // Step 2: Try a risky operation (rollback only this part)
                updateBalance(conn, "Alice", -2000.0); // Oops, too much
                double balance = getBalance(conn, "Alice");
                if (balance < 0) {
                    conn.rollback(sp); // Rollback to savepoint — bonus is kept!
                    System.out.println("  Rolled back to savepoint (negative balance prevented)");
                }

                conn.commit(); // Commit the bonus only
                System.out.println("  Committed with bonus intact");
            } catch (SQLException e) {
                conn.rollback();
            }
        }
    }

    /** Shows different isolation levels and their behavior */
    private static void isolationLevelDemo() throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, "sa", "")) {
            System.out.println("  Available isolation levels:");
            System.out.println("    TRANSACTION_READ_UNCOMMITTED = " + Connection.TRANSACTION_READ_UNCOMMITTED);
            System.out.println("    TRANSACTION_READ_COMMITTED   = " + Connection.TRANSACTION_READ_COMMITTED);
            System.out.println("    TRANSACTION_REPEATABLE_READ  = " + Connection.TRANSACTION_REPEATABLE_READ);
            System.out.println("    TRANSACTION_SERIALIZABLE     = " + Connection.TRANSACTION_SERIALIZABLE);
            System.out.println("  Current level: " + conn.getTransactionIsolation());

            // Set isolation level (must be done before transaction starts)
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            System.out.println("  Changed to SERIALIZABLE: " + conn.getTransactionIsolation());

            System.out.println("""
                
                  Interview tip:
                  - READ COMMITTED (PostgreSQL default): sees only committed data
                  - REPEATABLE READ (MySQL default): snapshot at transaction start
                  - SERIALIZABLE: full isolation, detects phantom reads, slowest
                  - Choose based on: correctness needs vs throughput requirements
                """);
        }
    }

    // --- Helper methods ---

    private static void setupSchema() throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, "sa", "");
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE accounts (
                    name VARCHAR(50) PRIMARY KEY,
                    balance DECIMAL(10,2) NOT NULL
                )
            """);
        }
    }

    private static void insertAccount(String name, double balance) throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, "sa", "");
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO accounts (name, balance) VALUES (?, ?)")) {
            ps.setString(1, name);
            ps.setDouble(2, balance);
            ps.executeUpdate();
        }
    }

    private static void updateBalance(Connection conn, String name, double delta) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE accounts SET balance = balance + ? WHERE name = ?")) {
            ps.setDouble(1, delta);
            ps.setString(2, name);
            ps.executeUpdate();
        }
    }

    private static double getBalance(Connection conn, String name) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT balance FROM accounts WHERE name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble("balance") : 0;
            }
        }
    }

    private static void printBalances(String label) throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, "sa", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name, balance FROM accounts ORDER BY name")) {
            System.out.println("  [" + label + "]");
            while (rs.next()) {
                System.out.printf("    %s: $%.2f%n", rs.getString("name"), rs.getDouble("balance"));
            }
        }
    }
}
