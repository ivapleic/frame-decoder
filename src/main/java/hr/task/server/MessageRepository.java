package hr.task.server;

import hr.task.model.TransactionMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class MessageRepository implements AutoCloseable {

    private static final String INSERT_SQL =
            "INSERT INTO transactions (transaction_type, transaction_id, amount, currency) "
                    + "VALUES (?, ?, ?, ?)";

    private final Connection connection;

    /**
     * Connects to the database and creates the table if it doesn't exist yet.
     * Example URL: jdbc:sqlite:transactions.db
     */
    public MessageRepository(String jdbcUrl) throws SQLException {
        connection = DriverManager.getConnection(jdbcUrl);
        try (Statement statement = connection.createStatement()) {
            statement.execute(loadSchema());
        } catch (SQLException | RuntimeException e) {
            connection.close();
            throw e;
        }
    }

    public void save(TransactionMessage message) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, message.type());
            statement.setString(2, message.id());
            statement.setString(3, message.amount().toPlainString());
            statement.setString(4, message.currency());
            statement.executeUpdate();
        }
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }

    private static String loadSchema() {
        try (InputStream in = MessageRepository.class.getResourceAsStream("/db-schema.sql")) {   // <- changed
            if (in == null) {
                throw new IllegalStateException("db-schema.sql not found on classpath");          // <- changed
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}