package logicStructure.specialException;

public class InvalidRangeException extends RuntimeException {
    public InvalidRangeException(String message) {
        super(message);
    }

    public InvalidRangeException(String message, String name) {
        super("Range \"%s\": " + message);
    }
}
