package hr.task.protocol;
import java.io.IOException;

public class InvalidFrameException extends IOException {
    public InvalidFrameException(String message) {
        super(message);
    }
}
