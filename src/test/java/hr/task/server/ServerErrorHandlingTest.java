package hr.task.server;

import hr.task.protocol.FrameWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServerErrorHandlingTest {

    @TempDir
    Path tempDir;

    @Test
    void invalidFrameStopsReadingButKeepsEarlierMessages() throws Exception {
        String url = "jdbc:sqlite:" + tempDir.resolve("test.db");
        StringWriter out = new StringWriter();

        try (MessageRepository repository = new MessageRepository(url)) {
            byte[] zeroLength = {0x00, 0x00};   // invalid frame
            var in = input(frame("PAYMENT|1|1.00|EUR"), zeroLength, frame("REFUND|2|2.00|EUR"));

            ServerMain.handleClient(in, out, repository);
        }

        // only the message before the invalid frame is stored, the one after it is not read
        assertEquals("PAYMENT|1|1.00|EUR\n", out.toString());
        assertEquals(List.of("PAYMENT|1|1.00|EUR"), MessageRepositoryTest.readRows(url));
    }

    @Test
    void messageThatIsNotATransactionDoesNotStopTheServer() throws Exception {
        String url = "jdbc:sqlite:" + tempDir.resolve("test.db");
        StringWriter out = new StringWriter();

        try (MessageRepository repository = new MessageRepository(url)) {
            var in = input(frame("END"), frame("PAYMENT|1|1.00|EUR"));

            ServerMain.handleClient(in, out, repository);
        }

        // END is only in the file, the next message is still processed
        assertEquals("END\nPAYMENT|1|1.00|EUR\n", out.toString());
        assertEquals(List.of("PAYMENT|1|1.00|EUR"), MessageRepositoryTest.readRows(url));
    }

    @Test
    void databaseErrorStopsProcessingAndWritesNothing() throws Exception {
        String url = "jdbc:sqlite:" + tempDir.resolve("test.db");
        MessageRepository repository = new MessageRepository(url);
        repository.close();   // the database is no longer usable
        StringWriter out = new StringWriter();

        assertThrows(SQLException.class,
                () -> ServerMain.handleClient(input(frame("PAYMENT|1|1.00|EUR")), out, repository));

        // nothing was written, so the file and the database stay in sync
        assertEquals("", out.toString());
    }

    /** One frame as bytes: 2-byte length + the text in ISO-8859-2. */
    private static byte[] frame(String text) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        FrameWriter.writeFrame(bytes, text.getBytes(Charset.forName("ISO-8859-2")));
        return bytes.toByteArray();
    }

    /** Joins frames into one "network stream". */
    private static ByteArrayInputStream input(byte[]... frames) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        for (byte[] frame : frames) {
            bytes.writeBytes(frame);
        }
        return new ByteArrayInputStream(bytes.toByteArray());
    }
}