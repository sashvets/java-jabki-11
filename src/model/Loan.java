package model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Loan {
    private final int id;
    private final int bookId;
    private final int userId;
    private final LocalDate loanDate;
    private LocalDate returnDate = null;

    private Library library;

    private static final AtomicInteger counter = new AtomicInteger(0);
    private static final int EXPIRED_DAYS_LIMIT = 30;

    Loan(int id, int bookId, int userId, LocalDate loanDate, LocalDate returnDate) {
        this.id = id;
        counter.accumulateAndGet(this.id, Math::max);
        this.bookId = bookId;
        this.userId = userId;
        this.loanDate = loanDate;
        this.returnDate = returnDate;
    }

    Loan(int bookId, int userId, LocalDate loanDate, Library library) {
        this(nextId(), bookId, userId, loanDate, null);
        this.library = library;
    }

    public int getId() {
        return this.id;
    }

    public int getBookId() {
        return this.bookId;
    }

    public int getUserId() {
        return this.userId;
    }

    public LocalDate getLoanDate() {
        return this.loanDate;
    }

    public LocalDate getReturnDate() {
        return this.returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    static int nextId() {
        return counter.incrementAndGet();
    }

    public void setLibrary(Library library) {
        this.library = library;
    }

    public boolean isExpired() {
        if (this.returnDate != null) {
            return false;
        }
        return loanDate.plusDays(EXPIRED_DAYS_LIMIT).isBefore(LocalDate.now());
    }

    public boolean isActive() {
        return this.returnDate == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Loan loan)) return false;
        return this.bookId == loan.bookId &&
                this.userId == loan.userId &&
                Objects.equals(this.loanDate, loan.loanDate) &&
                Objects.equals(this.returnDate, loan.returnDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.bookId, this.userId, this.loanDate, this.returnDate);
    }

    @Override
    public String toString() {
        String bookInfo;
        String userInfo;
        if (this.library != null) {
            bookInfo = this.library.getBook(this.bookId) != null ? this.library.getBook(this.bookId).toString() : "ID " + this.bookId;
            userInfo = this.library.getUser(this.userId) != null ? this.library.getUser(this.userId).toString() : "ID " + this.userId;
        } else {
            bookInfo = "ID " + this.bookId;
            userInfo = "ID " + this.userId;
        }
        return String.format("Читатель: %s\nКнига: %s\nID : %d, Выдана: %s, Возврат: %s",
                userInfo,
                bookInfo,
                this.id,
                this.loanDate,
                this.returnDate != null ? this.returnDate : !this.isExpired() ? "На руках" : "Просрочена");
    }

    public String toFileString() {
        return String.format("%d;%d;%d;%s;%s",
                this.id,
                this.bookId,
                this.userId,
                this.loanDate,
                this.returnDate != null ? this.returnDate : "");
    }

    public static Loan fromFileString(String line) {
        String[] parts = line.split(";", -1);
        if (parts.length != 5) {
            throw new IllegalArgumentException("Некорректная запись выдачи: " + line);
        }
        int id = Integer.parseInt(parts[0]);
        int bookId = Integer.parseInt(parts[1]);
        int userId = Integer.parseInt(parts[2]);
        LocalDate loanDate = LocalDate.parse(parts[3]);
        LocalDate returnDate = parts[4].isEmpty() ? null : LocalDate.parse(parts[4]);
        return new Loan(id, bookId, userId, loanDate, returnDate);
    }
}
