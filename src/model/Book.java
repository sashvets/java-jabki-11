package model;

import exceptions.BookValidationException;
import exceptions.NoBooksAvailableException;

import java.time.Year;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Book {
    private final int id;
    private final String title;
    private final String author;
    private final int year;
    private int totalCopies;
    private int availableCopies;
    private static final AtomicInteger counter = new AtomicInteger(0);

    public Book(int id, String title, String author, int year, int totalCopies, int availableCopies) {
        if (id <= 0) {
            throw new BookValidationException("ID книги должен быть положительным числом.");
        }
        if (title == null || title.isBlank()) {
            throw new BookValidationException("Название книги не может быть пустым.");
        }
        if (author == null || author.isBlank()) {
            throw new BookValidationException("Имя автора не может быть пустым.");
        }
        int currentYear = Year.now().getValue();
        if (year <= 0) {
            throw new BookValidationException("Год издания должен быть больше 0.");
        } else if (year > currentYear) {
            throw new BookValidationException("Год издания не может быть больше текущего года (" + currentYear + ").");
        }
        if (totalCopies < 0) {
            throw new BookValidationException("Количество экземпляров книги не может быть отрицательным.");
        }
        if (totalCopies < availableCopies) {
            throw new BookValidationException("Количество доступных экземпляров книги не может превышать имеющиеся.");
        }
        this.id = id;
        counter.accumulateAndGet(this.id, Math::max);
        this.title = title;
        this.author = author;
        this.year = year;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    public Book(int id, String title, String author, int year, int totalCopies) {
        this(id, title, author, year, totalCopies, totalCopies);
    }

    public Book(String title, String author, int year, int totalCopies) {
        this(nextId(), title, author, year, totalCopies);
    }

    public int getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getAuthor() {
        return this.author;
    }

    public int getYear() {
        return this.year;
    }

    public int getTotalCopies() {
        return this.totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        if (totalCopies < 0) {
            throw new BookValidationException("Количество экземпляров книги не может быть отрицательным.");
        }
        if (totalCopies < this.totalCopies) {
            throw new BookValidationException("Нельзя уменьшить количество экземпляров книги.");
        }

        int difference = totalCopies - this.totalCopies;
        this.totalCopies = totalCopies;
        this.availableCopies += difference;
    }

    public int getAvailableCopies() {
        return this.availableCopies;
    }

    public boolean giveBook() {
        if (this.availableCopies <= 0) {
            throw new NoBooksAvailableException();
        }
        this.availableCopies--;
        return true;
    }

    public boolean returnBook() {
        if (this.availableCopies < this.totalCopies) {
            this.availableCopies++;
            return true;
        }
        return false;
    }

    static int nextId() {
        return counter.incrementAndGet();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book book)) return false;
        return year == book.year &&
                title.equalsIgnoreCase(book.title) &&
                author.equalsIgnoreCase(book.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title.toLowerCase(), author.toLowerCase(), year);
    }

    @Override
    public String toString() {
        return String.format("ID: %d, Название: %s, Автор: %s, Год: %d, Доступно: %d/%d",
                this.id,
                this.title,
                this.author,
                this.year,
                this.availableCopies,
                this.totalCopies);
    }

    public String toFileString() {
        return String.format("%d;%s;%s;%d;%d;%d",
                this.id,
                this.title,
                this.author,
                this.year,
                this.totalCopies,
                this.availableCopies);
    }

    public static Book fromFileString(String line) {
        String[] parts = line.split(";");
        if (parts.length != 6) {
            throw new IllegalArgumentException("Некорректная запись книги: " + line);
        }

        int id = Integer.parseInt(parts[0]);
        String title = parts[1];
        String author = parts[2];
        int year = Integer.parseInt(parts[3]);
        int totalCopies = Integer.parseInt(parts[4]);
        int availableCopies = Integer.parseInt(parts[5]);

        return new Book(id, title, author, year, totalCopies, availableCopies);
    }
}
