import exceptions.BookValidationException;
import exceptions.NoBooksAvailableException;
import model.Book;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Year;

class BookTest {
    @Test
    void testBookConstructor() {
        Book book = new Book(1, "Колобок", "Дедуля", 2001, 3);
        Assertions.assertEquals(1, book.getId());
        Assertions.assertEquals("Колобок", book.getTitle());
        Assertions.assertEquals("Дедуля", book.getAuthor());
        Assertions.assertEquals(2001, book.getYear());
        Assertions.assertEquals(3, book.getTotalCopies());
        Assertions.assertEquals(3, book.getAvailableCopies());

        Exception exception;
        exception = Assertions.assertThrows(BookValidationException.class, () ->
                new Book(0, "Колобок", "Дедуля", 2001, 3));
        Assertions.assertEquals("ID книги должен быть положительным числом.", exception.getMessage());
        exception = Assertions.assertThrows(BookValidationException.class, () ->
                new Book(1, null, "Дедуля", 2001, 3));
        Assertions.assertEquals("Название книги не может быть пустым.", exception.getMessage());
        exception = Assertions.assertThrows(BookValidationException.class, () ->
                new Book(1, "Колобок", "", 2001, 3));
        Assertions.assertEquals("Имя автора не может быть пустым.", exception.getMessage());
        exception = Assertions.assertThrows(BookValidationException.class, () ->
                new Book(1, "Колобок", "Дедуля", -100, 3));
        Assertions.assertEquals("Год издания должен быть больше 0.", exception.getMessage());
        int currentYear = Year.now().getValue();
        exception = Assertions.assertThrows(BookValidationException.class, () ->
                new Book(1, "Колобок", "Дедуля", 2030, 3));
        Assertions.assertEquals("Год издания не может быть больше текущего года (" + currentYear + ").", exception.getMessage());
        exception = Assertions.assertThrows(BookValidationException.class, () ->
                new Book(1, "Колобок", "Дедуля", 2001, -2));
        Assertions.assertEquals("Количество экземпляров книги не может быть отрицательным.", exception.getMessage());
        exception = Assertions.assertThrows(BookValidationException.class, () ->
                new Book(1, "Колобок", "Дедуля", 2001, 2, 3));
        Assertions.assertEquals("Количество доступных экземпляров книги не может превышать имеющиеся.", exception.getMessage());
    }

    @Test
    void testGiveBook() {
        Book book = new Book(1, "Колобок", "Дедуля", 2001, 1);
        Assertions.assertTrue(book.giveBook());
        Exception exception;
        exception = Assertions.assertThrows(NoBooksAvailableException.class, book::giveBook);
        Assertions.assertEquals("Нет доступных экземпляров книги.", exception.getMessage());
    }

    @Test
    void testReturnBook() {
        Book book = new Book(1, "Колобок", "Дедуля", 2001, 1);
        book.giveBook();
        Assertions.assertTrue(book.returnBook());
        Assertions.assertFalse(book.returnBook());
        Assertions.assertEquals(1, book.getAvailableCopies());
    }

    @Test
    void testEqualsAndHashCode() {
        Book book1 = new Book(1, "Колобок", "Дедуля", 2001, 3);
        Book book2 = new Book(7, "колобок", "дедуля", 2001, 1);
        Assertions.assertEquals(book1, book2);
        Assertions.assertEquals(book1.hashCode(), book2.hashCode());
    }

    @Test
    void testToStringFormat() {
        Book book = new Book(1, "Колобок", "Дедуля", 2001, 1);
        Assertions.assertEquals("ID: 1, Название: Колобок, Автор: Дедуля, Год: 2001, Доступно: 1/1", book.toString());
    }

    @Test
    void testToFileStringAndFromFileString() {
        Book book = new Book(1, "Колобок", "Дедуля", 2001, 1);
        String lineBook = book.toFileString();
        Assertions.assertEquals("1;Колобок;Дедуля;2001;1;1", lineBook);
        Book bookFromLine = Book.fromFileString(lineBook);
        Assertions.assertEquals(book, bookFromLine);
        Assertions.assertEquals(book.hashCode(), bookFromLine.hashCode());
        Assertions.assertEquals(book.getTotalCopies(), bookFromLine.getTotalCopies());
        Assertions.assertEquals(book.getAvailableCopies(), bookFromLine.getAvailableCopies());
    }
}