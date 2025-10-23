package exceptions;

public class UserBookQuotaExceededException extends RuntimeException {
    public UserBookQuotaExceededException() {
        super("Превышен лимит по выдаче книг. У читателя уже имеется 3 книги на руках.");
    }
}
