package hr.task.protocol;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FrameReaderTest {

    /** Pomoćna funkcija: od zadanih brojeva (0-255) napravi "lažnu datoteku". */
    private static ByteArrayInputStream bytes(int... values) {
        byte[] data = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            data[i] = (byte) values[i];
        }
        return new ByteArrayInputStream(data);
    }

    @Test
    void readsSingleFrame() throws Exception {
        // 00 03 | 45 4E 44  ->  "END"
        var in = bytes(0x00, 0x03, 0x45, 0x4E, 0x44);

        byte[] payload = FrameReader.readFrame(in);

        assertArrayEquals(new byte[]{0x45, 0x4E, 0x44}, payload);
    }

    @Test
    void readsTwoFramesInOrderThenReturnsNull() throws Exception {
        var in = bytes(0x00, 0x01, 0x41,
                0x00, 0x02, 0x42, 0x43);

        assertArrayEquals(new byte[]{0x41}, FrameReader.readFrame(in));
        assertArrayEquals(new byte[]{0x42, 0x43}, FrameReader.readFrame(in));
        assertNull(FrameReader.readFrame(in));   // normalan kraj
    }

    @Test
    void emptyStreamIsNormalEnd() throws Exception {
        assertNull(FrameReader.readFrame(bytes()));
    }

    @Test
    void zeroLengthIsInvalid() {
        var in = bytes(0x00, 0x00);

        assertThrows(InvalidFrameException.class, () -> FrameReader.readFrame(in));
    }

    @Test
    void lengthAboveMaximumIsInvalid() {
        // 0x1001 = 4097 > 4096
        var in = bytes(0x10, 0x01, 0x41);

        assertThrows(InvalidFrameException.class, () -> FrameReader.readFrame(in));
    }

    @Test
    void truncatedHeaderIsInvalid() {
        // samo 1 bajt zaglavlja
        var in = bytes(0x00);

        assertThrows(InvalidFrameException.class, () -> FrameReader.readFrame(in));
    }

    @Test
    void truncatedPayloadIsInvalid() {
        // obećano 5 bajtova, stigla 2
        var in = bytes(0x00, 0x05, 0x41, 0x42);

        assertThrows(InvalidFrameException.class, () -> FrameReader.readFrame(in));
    }
}