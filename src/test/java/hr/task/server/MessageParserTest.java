package hr.task.server;

import hr.task.model.TransactionMessage;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MessageParserTest {

    @Test
    void parsesValidMessage() {
        TransactionMessage message = MessageParser.parse("PAYMENT|12345|100.50|HRK");

        assertEquals(new TransactionMessage("PAYMENT", "12345", new BigDecimal("100.50"), "HRK"), message);
    }

    @Test
    void acceptsCroatianLettersInId() {
        TransactionMessage message = MessageParser.parse("UPLATA|ČĆŽŠĐ|100.50|HRK");

        assertEquals("ČĆŽŠĐ", message.id());
    }

    @Test
    void terminatorMessageIsNotATransaction() {
        assertThrows(IllegalArgumentException.class, () -> MessageParser.parse("END"));
    }

    @Test
    void tooManyFieldsIsInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> MessageParser.parse("PAYMENT|12345|100.50|HRK|extra"));
    }

    @Test
    void emptyFieldIsInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> MessageParser.parse("|12345|100.50|HRK"));
    }

    @Test
    void invalidAmountIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> MessageParser.parse("PAYMENT|12345|abc|HRK"));
    }

    @Test
    void currencyMustHaveThreeCharacters() {
        assertThrows(IllegalArgumentException.class,
                () -> MessageParser.parse("PAYMENT|12345|100.50|HR"));
    }
}