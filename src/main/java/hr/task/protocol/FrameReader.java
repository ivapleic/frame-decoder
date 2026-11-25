package hr.task.protocol;
import java.io.IOException;
import java.io.InputStream;

public final class FrameReader {
    public static final int MAX_PAYLOAD = 4096;

    private FrameReader() {
    }

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

        if (length < 1 || length > MAX_PAYLOAD) {
            throw new InvalidFrameException(
                    "Invalid payload length " + length + " (allowed 1 to " + MAX_PAYLOAD + ")");
        }

        byte[] payload = in.readNBytes(length);
        if (payload.length < length) {
            throw new InvalidFrameException(
                    "Truncated payload: expected " + length + " bytes, got " + payload.length);
        }

        return payload;
    }
}
