package hr.task.client;

import hr.task.protocol.FrameReader;
import hr.task.protocol.FrameWriter;
import hr.task.protocol.InvalidFrameException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClientMain {

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: ClientMain <inputFile> <host> <port>");
            System.exit(1);
        }

        Path inputFile = Path.of(args[0]);
        String host = args[1];
        int port;
        try {
            port = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            System.err.println("Port must be a number, got: " + args[2]);
            System.exit(1);
            return;
        }

        try {
            int sent = sendFile(inputFile, host, port);
            System.out.println("Done, sent " + sent + " frames.");
        } catch (InvalidFrameException e) {
            System.err.println("Bad input file: " + e.getMessage());
            System.exit(2);
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            System.exit(3);
        }
    }

    /**
     * Reads frames from the file one by one and sends them to the server.
     * Returns how many frames were sent.
     */
    public static int sendFile(Path inputFile, String host, int port) throws IOException {
        int count = 0;

        try (InputStream in = new BufferedInputStream(Files.newInputStream(inputFile));
             Socket socket = new Socket(host, port)) {

            OutputStream out = socket.getOutputStream();

            byte[] payload;
            while ((payload = FrameReader.readFrame(in)) != null) {
                FrameWriter.writeFrame(out, payload);
                count++;
            }
        }

        return count;
    }
}