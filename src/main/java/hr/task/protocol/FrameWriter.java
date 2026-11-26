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

        // same rule as the reader, so we never send an invalid frame
        FrameReader.checkLength(length);

        out.write(length >> 8);      // high byte
        out.write(length & 0xFF);    // low byte
        out.write(payload);
        out.flush();
    }
}