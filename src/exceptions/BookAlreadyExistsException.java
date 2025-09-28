package exceptions;

import model.Book;

public class BookAlreadyExistsException extends RuntimeException {
    public BookAlreadyExistsException(Book book) {
        super(String.format(
                "Книга \"%s\" автора %s (%d) уже есть в библиотеке.",
                book.getTitle(),
                book.getAuthor(),
                book.getYear()
        ));
    }
}
