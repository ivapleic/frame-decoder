package hr.task.protocol;

import java.io.IOException;
import java.io.InputStream;

public final class FrameReader {

    public static final int MAX_PAYLOAD = 4096;

    private FrameReader() {
    }

    /**
     * Reads one frame. Returns the payload, or null if the stream ended
     * before a new frame started (normal end).
     */
    public static byte[] readFrame(InputStream in) throws IOException {
        int first = in.read();
        if (first == -1) {
            return null;
        }

        int second = in.read();
        if (second == -1) {
            throw new InvalidFrameException("Truncated header: expected 2 bytes, got 1");
        }

        int length = (first << 8) | second;
        checkLength(length);

        byte[] payload = in.readNBytes(length);
        if (payload.length < length) {
            throw new InvalidFrameException(
                    "Truncated payload: expected " + length + " bytes, got " + payload.length);
        }

        return payload;
    }

    /**
     * Protocol rule for the payload size (1 to 4096 bytes).
     * Used by the reader and the writer, so the rule lives in one place.
     */
    static void checkLength(int length) throws InvalidFrameException {
        if (length < 1 || length > MAX_PAYLOAD) {
            throw new InvalidFrameException(
                    "Invalid payload length " + length + " (allowed 1 to " + MAX_PAYLOAD + ")");
        }
    }
}