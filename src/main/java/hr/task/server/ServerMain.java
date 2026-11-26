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


public class ServerMain {

    private static final Charset INPUT_CHARSET = Charset.forName("ISO-8859-2");

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: ServerMain <port> <outputFile>");
            System.exit(1);
        }

        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("Port must be a number, got: " + args[0]);
            System.exit(1);
            return;
        }
        Path outputFile = Path.of(args[1]);

        try {
            run(port, outputFile);
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            System.exit(3);
        }
    }

    /**
     * Opens the server socket and the output file, then serves clients.
     * The output file is created (or emptied) when the server starts.
     */
    static void run(int port, Path outputFile) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port);
             Writer out = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {

            System.out.println("Listening on port " + port + ", writing to " + outputFile);
            serve(serverSocket, out);
        }
    }

    /**
     * Handles clients one after another until the server socket is closed.
     */
    static void serve(ServerSocket serverSocket, Writer out) throws IOException {
        while (!serverSocket.isClosed()) {
            try {
                acceptOne(serverSocket, out);
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
    static void acceptOne(ServerSocket serverSocket, Writer out) throws IOException {
        try (Socket socket = serverSocket.accept()) {
            System.out.println("Client connected");
            handleClient(socket.getInputStream(), out);
        }
    }

    /**
     * Reads frames from one client until it is done or sends something invalid.
     * Every message is decoded (ISO-8859-2) and written as one UTF-8 line.
     */
    static void handleClient(InputStream in, Writer out) {
        try {
            byte[] payload;
            while ((payload = FrameReader.readFrame(in)) != null) {
                String text = new String(payload, INPUT_CHARSET);
                out.write(text);
                out.write('\n');
                out.flush();   // so the file is complete even while the server keeps running
            }
            System.out.println("Client finished");
        } catch (InvalidFrameException e) {
            System.err.println("Invalid frame: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        }
    }
}