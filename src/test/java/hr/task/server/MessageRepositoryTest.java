package hr.task.server;

import hr.task.model.TransactionMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void savesMessagesInReceivedOrder() throws Exception {
        String url = "jdbc:sqlite:" + tempDir.resolve("test.db");

        try (MessageRepository repository = new MessageRepository(url)) {
            repository.save(new TransactionMessage("PAYMENT", "12345", new BigDecimal("100.50"), "HRK"));
            repository.save(new TransactionMessage("UPLATA", "ČĆŽŠĐ", new BigDecimal("42.00"), "EUR"));
        }

        assertEquals(List.of(
                "PAYMENT|12345|100.50|HRK",
                "UPLATA|ČĆŽŠĐ|42.00|EUR"), readRows(url));
    }

    @Test
    void openingTheSameDatabaseTwiceKeepsOldRows() throws Exception {
        String url = "jdbc:sqlite:" + tempDir.resolve("test.db");

        try (MessageRepository repository = new MessageRepository(url)) {
            repository.save(new TransactionMessage("PAYMENT", "1", new BigDecimal("1.00"), "EUR"));
        }
        try (MessageRepository repository = new MessageRepository(url)) {
            repository.save(new TransactionMessage("REFUND", "2", new BigDecimal("2.00"), "EUR"));
        }

        assertEquals(List.of("PAYMENT|1|1.00|EUR", "REFUND|2|2.00|EUR"), readRows(url));
    }

    /** Reads the rows with a separate connection, the way someone else would look at the table. */
    private static List<String> readRows(String url) throws SQLException {
        List<String> rows = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url);
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT transaction_type, transaction_id, amount, currency "
                             + "FROM transactions ORDER BY id")) {
            while (result.next()) {
                rows.add(result.getString(1) + "|" + result.getString(2) + "|"
                        + result.getString(3) + "|" + result.getString(4));
            }
        }
        return rows;
    }
}