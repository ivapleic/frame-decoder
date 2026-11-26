package hr.task.server;

import hr.task.protocol.FrameReader;
import hr.task.protocol.InvalidFrameException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

public class ServerMain {

    private static final Charset INPUT_CHARSET = Charset.forName("ISO-8859-2");

    private static final int DEFAULT_PORT = 5000;
    private static final String DEFAULT_OUTPUT_FILE = "output.txt";
    private static final String DEFAULT_DATABASE_FILE = "transactions.db";

    public static void main(String[] args) {
        if (args.length > 3) {
            System.err.println("Usage: ServerMain [port] [outputFile] [databaseFile]");
            System.err.println("Defaults: " + DEFAULT_PORT + " " + DEFAULT_OUTPUT_FILE
                    + " " + DEFAULT_DATABASE_FILE);
            System.exit(1);
        }

        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Port must be a number, got: " + args[0]);
                System.exit(1);
                return;
            }
        }
        Path outputFile = Path.of(args.length > 1 ? args[1] : DEFAULT_OUTPUT_FILE);
        Path databaseFile = Path.of(args.length > 2 ? args[2] : DEFAULT_DATABASE_FILE);

        try {
            run(port, outputFile, databaseFile);
        } catch (IOException | SQLException e) {
            System.err.println("Server error: " + e.getMessage());
            System.exit(3);
        }
    }

    /**
     * Opens the server socket, the output file and the database, then serves clients.
     * The output file is emptied on start, the database keeps rows from earlier runs.
     */
    static void run(int port, Path outputFile, Path databaseFile) throws IOException, SQLException {
        try (ServerSocket serverSocket = new ServerSocket(port);
             Writer out = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8);
             MessageRepository repository = new MessageRepository("jdbc:sqlite:" + databaseFile)) {

            System.out.println("Listening on port " + port
                    + ", writing to " + outputFile + " and " + databaseFile);
            serve(serverSocket, out, repository);
        }
    }

    /**
     * Handles clients one after another until the server socket is closed.
     */
    static void serve(ServerSocket serverSocket, Writer out, MessageRepository repository) throws IOException {
        while (!serverSocket.isClosed()) {
            try {
                acceptOne(serverSocket, out, repository);
            } catch (SocketException e) {
                if (serverSocket.isClosed()) {
                    return;   // closed on purpose, that's a normal stop
                }
                throw e;
            }
        }
    }

    /**
     * Waits for one client and handles it until it is done.
     */
    static void acceptOne(ServerSocket serverSocket, Writer out, MessageRepository repository) throws IOException {
        try (Socket socket = serverSocket.accept()) {
            System.out.println("Client connected");
            handleClient(socket.getInputStream(), out, repository);
        }
    }

    /**
     * Reads frames from one client until it is done or sends something invalid.
     * Every message is decoded (ISO-8859-2), written as one UTF-8 line and stored in the database.
     */
    static void handleClient(InputStream in, Writer out, MessageRepository repository) {
        try {
            byte[] payload;
            while ((payload = FrameReader.readFrame(in)) != null) {
                String text = new String(payload, INPUT_CHARSET);
                out.write(text);
                out.write('\n');
                out.flush();   // so the file is complete even while the server keeps running

                storeInDatabase(text, repository);
            }
            System.out.println("Client finished");
        } catch (InvalidFrameException e) {
            System.err.println("Invalid frame: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        }
    }

    /**
     * Messages that are not 4-field transactions (like END or KRAJ) stay only in the file.
     */
    private static void storeInDatabase(String text, MessageRepository repository) {
        try {
            repository.save(MessageParser.parse(text));
        } catch (IllegalArgumentException e) {
            System.err.println("Not stored in database: " + e.getMessage() + " (message: " + text + ")");
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }
}