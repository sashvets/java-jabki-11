package model;

import exceptions.BookAlreadyExistsException;
import exceptions.UserAlreadyExistsException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Library {
    public Map<Integer, Book> books;
    public Map<Integer, User> users;

    public Map<Integer, Map<Integer, Integer>> userBooks;
    private final String booksFile;
    private final String usersFile;
    private final String borrowsFile;

    public Library(String bookFile, String userFile, String borrowsFile) {
        this.booksFile = bookFile;
        this.usersFile = userFile;
        this.borrowsFile = borrowsFile;
        this.books = new HashMap<Integer, Book>();
        this.users = new HashMap<Integer, User>();
        this.userBooks = new HashMap<>();
        if (this.booksFile != null) {
            if (!this.booksFile.isBlank()) {
                try {
                    this.loadBooks();
                } catch (IOException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }
        if (this.usersFile != null) {
            if (!this.usersFile.isBlank()) {
                try {
                    this.loadUsers();
                } catch (IOException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }
        if (this.borrowsFile != null) {
            if (!this.borrowsFile.isBlank()) {
                try {
                    this.loadBorrows();
                } catch (IOException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }
    }

    public void addBook(Book book) {
        boolean duplicate = books.values().stream()
                .anyMatch(b -> b.equals(book));
        if (duplicate) {
            throw new BookAlreadyExistsException(book);
        }
        this.books.put(book.getId(), book);
        try {
            this.saveBooks();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void addUser(User user) {
        boolean duplicate = users.values().stream()
                .anyMatch(b -> b.equals(user));
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

    // Сохранение списка пользователй в файл
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
                this.users.put(user.getId(), user);
            }
        }
    }

    // Выдача книги
    public void borrowBook(int userId, int bookId) {
        User user = users.get(userId);
        Book book = books.get(bookId);

        if (user == null) {
            throw new IllegalArgumentException("Читатель с id=" + userId + " не найден.");
        }
        if (book == null) {
            throw new IllegalArgumentException("Книга с id=" + bookId + " не найдена.");
        }

        book.giveBook();

        this.userBooks.computeIfAbsent(userId, k -> new HashMap<>());
        Map<Integer, Integer> booksOfUser = this.userBooks.get(userId);
        booksOfUser.put(bookId, booksOfUser.getOrDefault(bookId, 0) + 1);
        try {
            this.saveBorrows();
            this.saveBooks();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    // Возврат книги
    public void returnBook(int userId, int bookId) {
        Map<Integer, Integer> booksOfUser = this.userBooks.get(userId);
        if (booksOfUser == null || !booksOfUser.containsKey(bookId)) {
            throw new IllegalArgumentException("У читателя нет книги с id=" + bookId);
        }

        int count = booksOfUser.get(bookId);
        if (count <= 1) {
            booksOfUser.remove(bookId);
        } else {
            booksOfUser.put(bookId, count - 1);
        }

        Book book = this.books.get(bookId);
        book.returnBook();

        if (booksOfUser.isEmpty()) {
            this.userBooks.remove(userId);
        }
        try {
            this.saveBorrows();
            this.saveBooks();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    // Получить книги выданные читателю
    public Map<Book, Integer> getUserBooks(int userId) {
        Map<Integer, Integer> booksOfUser = this.userBooks.get(userId);
        if (booksOfUser == null) return Map.of();

        Map<Book, Integer> result = new HashMap<>();
        for (var entry : booksOfUser.entrySet()) {
            result.put(this.books.get(entry.getKey()), entry.getValue());
        }
        return result;
    }

    // Сохранить выдачи в файл
    public void saveBorrows() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.borrowsFile))) {
            for (var userEntry : this.userBooks.entrySet()) {
                int userId = userEntry.getKey();
                for (var bookEntry : userEntry.getValue().entrySet()) {
                    int bookId = bookEntry.getKey();
                    int count = bookEntry.getValue();
                    writer.write(String.format("%d;%d;%d", userId, bookId, count));
                    writer.newLine();
                }
            }
        }
    }

    // Загрузить список книг на руках у читателей из фала
    public void loadBorrows() throws IOException {
        this.userBooks.clear();
        File file = new File(this.borrowsFile);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length != 3) continue;

                int userId = Integer.parseInt(parts[0]);
                int bookId = Integer.parseInt(parts[1]);
                int count = Integer.parseInt(parts[2]);

                this.userBooks.computeIfAbsent(userId, k -> new HashMap<>());
                this.userBooks.get(userId).put(bookId, count);
            }
        }
    }
}