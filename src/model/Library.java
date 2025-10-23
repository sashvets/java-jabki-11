package model;

import exceptions.BookFromUserNotFoundException;
import exceptions.BookNotFoundException;
import exceptions.BookValidationException;
import exceptions.UserAlreadyExistsException;
import exceptions.UserBookQuotaExceededException;
import exceptions.UserNotFoundException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Library {
    private final Map<Integer, Book> books;
    private final Map<Integer, User> users;
    private final Map<Integer, Loan> loans;

    private final String booksFile = "src/storage/books";
    private final String usersFile = "src/storage/users";
    private final String loansFile = "src/storage/loans";

    private static final int MAX_BOOKS_PER_USER = 3;

    public Library() {
        this.books = new HashMap<Integer, Book>();
        this.users = new HashMap<Integer, User>();
        this.loans = new HashMap<Integer, Loan>();
    }

    public void initLibrary() {
        try {
            this.loadBooks();
            this.loadUsers();
            this.loadLoans();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Map<Integer, Book> getBooks() {
        return this.books;
    }

    public Book getBook(int id) {
        return this.books.get(id);
    }

    public Map<Integer, Loan> getLoans() {
        return this.loans;
    }

    public Loan getLoan(int id) {
        return this.loans.get(id);
    }

    public Map<Integer, User> getUsers() {
        return this.users;
    }

    public User getUser(int id) {
        return this.users.get(id);
    }

    public int addBook(Book book) {
        for (Book existBook : this.books.values()) {
            if (existBook.equals(book)) {
                if (book.getTotalCopies() <= 0) {
                    throw new BookValidationException("Количество добавляемых книг не может быть меньше или равно нулю.");
                }
                existBook.setTotalCopies(existBook.getTotalCopies() + book.getTotalCopies());
                try {
                    saveBooks();
                } catch (IOException e) {
                    throw new IllegalArgumentException(e);
                }
                return existBook.getId();
            }
        }

        this.books.put(book.getId(), book);
        try {
            saveBooks();
            return book.getId();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void addUser(User user) {
        boolean duplicate = users.values().contains(user);
        if (duplicate) {
            throw new UserAlreadyExistsException(user);
        }
        this.users.put(user.getId(), user);
        try {
            this.saveUsers();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    // Сохранение списка книг в файл
    public void saveBooks() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(booksFile))) {
            for (Book book : books.values()) {
                writer.write(book.toFileString());
                writer.newLine();
            }
        }
    }

    // Сохранение списка пользователей в файл
    public void saveUsers() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.usersFile))) {
            for (User user : this.users.values()) {
                writer.write(user.toFileString());
                writer.newLine();
            }
        }
    }

    // Загрузка списка книг из файла
    private void loadBooks() throws IOException {
        this.books.clear();
        File file = new File(this.booksFile);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Book book = Book.fromFileString(line);
                this.books.put(book.getId(), book);
            }
        }
    }

    // Загрузка списка читателей из файла
    private void loadUsers() throws IOException {
        this.users.clear();
        File file = new File(this.usersFile);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                User user = User.fromFileString(line);
                user.setLibrary(this);
                this.users.put(user.getId(), user);
            }
        }
    }

    // Выдача книги
    public void borrowBook(int userId, int bookId) {
        long activeLoanCount = loans.values().stream()
                .filter(l -> l.getUserId() == userId && l.isActive())
                .count();

        if (activeLoanCount >= MAX_BOOKS_PER_USER) {
            throw new UserBookQuotaExceededException();
        }

        User user = this.users.get(userId);
        Book book = this.books.get(bookId);

        if (user == null) {
            throw new UserNotFoundException("Читатель с id=" + userId + " не найден.");
        }
        if (book == null) {
            throw new BookNotFoundException("Книга с id=" + bookId + " не найдена.");
        }

        //Уже у читателя
        boolean isActiveLoan = loans.values().stream()
                .anyMatch(l -> l.getBookId() == bookId &&
                        l.getUserId() == userId &&
                        l.isActive());
        if (isActiveLoan) {
            throw new IllegalStateException("Книга уже на руках у читателя.");
        }

        Loan loan = new Loan(bookId, userId, LocalDate.now(), this);
        this.loans.put(loan.getId(), loan);

        book.giveBook();
        try {
            this.saveLoans();
            this.saveBooks();
            System.out.println("Книга выдана:");
            System.out.println(loan);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    // Возврат книги
    public void returnBook(int userId, int bookId) {
        Loan loan = null;
        for (Loan l : this.loans.values()) {
            if (l.getUserId() == userId && l.getBookId() == bookId && l.isActive()) {
                loan = l;
                break;
            }
        }
        if (loan == null) {
            throw new BookFromUserNotFoundException("У читателя нет на руках книги с id=" + bookId);
        }

        loan.setReturnDate(LocalDate.now());
        Book book = books.get(bookId);
        book.returnBook();

        try {
            this.saveLoans();
            this.saveBooks();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    // Сохранить выдачи в файл
    public void saveLoans() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.loansFile))) {
            for (Loan loan : this.loans.values()) {
                writer.write(loan.toFileString());
                writer.newLine();
            }
        }
    }

    // Загрузить список книг на руках у читателей из файла
    public void loadLoans() throws IOException {
        this.loans.clear();
        File f = new File(this.loansFile);
        if (!f.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Loan loan = Loan.fromFileString(line);
                loan.setLibrary(this);
                this.loans.put(loan.getId(), loan);
            }
        }
    }

    //Поиск просроченных выдач (надо вернуть в течение 30 дней после получения)
    public List<Loan> getExpiredLoans() {
        return loans.values().stream()
                .filter(Loan::isExpired)
                .toList();
    }

    // Просмотр истории выдач: По конкретному пользователю
    public List<Loan> getUserLoanHistory(int userId) {
        return this.getUser(userId).getUserLoans();
    }

    //Просмотр истории выдач: По конкретной книге
    public List<Loan> getBookLoanHistory(int bookId) {
        return loans.values().stream()
                .filter(l -> l.getBookId() == bookId)
                .toList();
    }
}