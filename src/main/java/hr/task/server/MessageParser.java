package hr.task.server;

import hr.task.model.TransactionMessage;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public final class MessageParser {

    private static final int FIELD_COUNT = 4;
    private static final Pattern ALPHANUMERIC = Pattern.compile("[\\p{L}\\p{N}]+");
    private static final Pattern CURRENCY = Pattern.compile("[\\p{L}\\p{N}]{3}");

    private MessageParser() {
    }

    /**
     * Splits a decoded message into its 4 fields.
     * Throws IllegalArgumentException if the message doesn't look like a transaction.
     */
    public static TransactionMessage parse(String line) {
        String[] fields = line.split("\\|", -1);
        if (fields.length != FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "Expected " + FIELD_COUNT + " fields separated by '|', got " + fields.length);
        }

        String type = fields[0];
        String id = fields[1];
        String currency = fields[3];

        requireMatch(type, ALPHANUMERIC, "transaction type");
        requireMatch(id, ALPHANUMERIC, "transaction ID");
        requireMatch(currency, CURRENCY, "currency");

        BigDecimal amount;
        try {
            amount = new BigDecimal(fields[2]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount: '" + fields[2] + "'");
        }

        return new TransactionMessage(type, id, amount, currency);
    }

    private static void requireMatch(String value, Pattern pattern, String fieldName) {
        if (!pattern.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid " + fieldName + ": '" + value + "'");
        }
    }
}
