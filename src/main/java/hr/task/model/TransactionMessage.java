package hr.task.model;

import java.math.BigDecimal;
import java.util.Objects;

public class TransactionMessage {

    private final String type;
    private final String id;
    private final BigDecimal amount;
    private final String currency;

    public TransactionMessage(String type, String id, BigDecimal amount, String currency) {
        this.type = type;
        this.id = id;
        this.amount = amount;
        this.currency = currency;
    }

    public String type() {
        return type;
    }

    public String id() {
        return id;
    }

    public BigDecimal amount() {
        return amount;
    }

    public String currency() {
        return currency;
    }

    // needed so two messages with the same fields count as equal (used in tests)
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TransactionMessage that = (TransactionMessage) o;
        return Objects.equals(type, that.type)
                && Objects.equals(id, that.id)
                && Objects.equals(amount, that.amount)
                && Objects.equals(currency, that.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id, amount, currency);
    }

    // readable output, e.g. in test failure messages
    @Override
    public String toString() {
        return "TransactionMessage{type=" + type + ", id=" + id
                + ", amount=" + amount + ", currency=" + currency + "}";
    }
}