package exceptions;

public class BookValidationException extends RuntimeException {
    public BookValidationException(String message) {
        super(message);
    }
}
