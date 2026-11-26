package hr.task.server;

import hr.task.client.ClientMain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ClientServerIntegrationTest {

    @TempDir
    Path tempDir;   // JUnit makes a temporary folder and deletes it after the test

    @Test
    void croatianSampleIsConvertedToUtf8() throws Exception {
        runFlow(Path.of("sample-data/sample-input-hr.bin"), List.of(
                "UPLATA|ČĆŽŠĐ|100.50|HRK",
                "STORNO|žćčšđ|42.00|EUR",
                "KRAJ"));
    }

    @Test
    void asciiSampleIsWrittenUnchanged() throws Exception {
        runFlow(Path.of("sample-data/sample-input-ascii.bin"), List.of(
                "PAYMENT|12345|100.50|HRK",
                "REFUND|98765|42.00|EUR",
                "END"));
    }

    /**
     * Starts the server, sends the input file with the client
     * and checks the lines the server wrote to its output file.
     */
    private void runFlow(Path inputFile, List<String> expectedLines) throws Exception {
        Path outputFile = tempDir.resolve("output.txt");

        try (ServerSocket serverSocket = new ServerSocket(0);   // 0 = pick any free port
             Writer out = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {

            Thread serverThread = new Thread(() -> {
                try {
                    ServerMain.acceptOne(serverSocket, out);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            serverThread.start();

            ClientMain.sendFile(inputFile, "localhost", serverSocket.getLocalPort());

            serverThread.join(5000);   // returns as soon as the server has handled the client
            assertFalse(serverThread.isAlive(), "server did not finish in time");
        }

        // the writer is closed here, so the file is complete
        assertEquals(expectedLines, Files.readAllLines(outputFile, StandardCharsets.UTF_8));
    }
}