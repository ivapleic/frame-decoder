package hr.task.protocol;

import java.io.IOException;
import java.io.OutputStream;

public final class FrameWriter {

    private FrameWriter() {
    }

    /**
     * Writes one frame: a 2-byte length (big-endian) followed by the payload.
     */
    public static void writeFrame(OutputStream out, byte[] payload) throws IOException {
        int length = payload.length;

        // same limits as the reader, so we never send an invalid frame
        if (length < 1 || length > FrameReader.MAX_PAYLOAD) {
            throw new InvalidFrameException(
                    "Invalid payload length " + length + " (allowed 1 to " + FrameReader.MAX_PAYLOAD + ")");
        }

        out.write(length >> 8);      // high byte
        out.write(length & 0xFF);    // low byte
        out.write(payload);
        out.flush();                 // send right away, don't wait in the buffer
    }
}