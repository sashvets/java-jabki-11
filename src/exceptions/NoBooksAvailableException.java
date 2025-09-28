package exceptions;

public class NoBooksAvailableException extends RuntimeException {
    public NoBooksAvailableException() {
        super("Нет доступных экземпляров книги.");
    }
}
