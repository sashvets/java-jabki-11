package exceptions;

public class BookFromUserNotFoundException extends RuntimeException {
    public BookFromUserNotFoundException(String message) {
        super(message);
    }
}
