package ui;

import model.Book;
import model.Library;
import model.User;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class LibraryUI {
    private final Library library;
    private final Scanner scanner = new Scanner(System.in);

    public LibraryUI() {
        this.library = new Library();
        this.library.initLibrary();
    }

    public void start() {
        while (true) {
            System.out.print("\n********************\n" +
                    " 1. Добавить книгу\n" +
                    " 2. Поиск книги\n" +
                    " 3. Все книги\n" +
                    " 4. Добавить читателя\n" +
                    " 5. Поиск читателя\n" +
                    " 6. Все читатели\n" +
                    " 7. Выдать книгу\n" +
                    " 8. Вернуть книгу\n" +
                    " 9. Книги у читателя\n" +
                    "10. Все выданные книги\n" +
                    " 0. Выход\n" +
                    "Выберите пункт: "
            );
            String choice = scanner.nextLine();
            try {
                switch (choice) {
                    case "1" -> addLibraryBook();
                    case "2" -> searchBookAndPrint();
                    case "3" -> showAllBooks();
                    case "4" -> addLibraryUser();
                    case "5" -> searchUserAndPrint();
                    case "6" -> showAllUsers();
                    case "7" -> borrowBook();
                    case "8" -> returnBook();
                    case "9" -> showUserBooks();
                    case "10" -> showBorrowsBooks();
                    case "0" -> {
                        System.out.println("Выход...");
                        return;
                    }
                    default -> System.out.println("Неверный выбор. Повторите.");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                askReturn();
            }
        }
    }

    private void addLibraryBook() {
        System.out.println("*** Регистрация новой книги ***");
        System.out.print("Название: ");
        String title = scanner.nextLine();
        System.out.print("Автор: ");
        String author = scanner.nextLine();
        int year;
        while (true) {
            System.out.print("Год: ");
            String yearStr = scanner.nextLine().trim();
            try {
                year = Integer.parseInt(yearStr);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите корректный год.");
            }
        }
        int totalCopies;
        while (true) {
            System.out.print("Количество экземпляров: ");
            String totalCopiesStr = scanner.nextLine().trim();
            try {
                totalCopies = Integer.parseInt(totalCopiesStr);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите корректное количество экземпляров.");
            }
        }
        Book book = new Book(title, author, year, totalCopies);
        int bookId = library.addBook(book);
        System.out.println(library.getBooks().get(bookId));
        System.out.println("Книга добавлена!");
        askReturn();
    }

    private void addLibraryUser() {
        System.out.println("*** Регистрация нового читателя ***");
        System.out.print("Имя: ");
        String name = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        User newUser = new User(name, email);
        library.addUser(newUser);
        System.out.println(newUser);
        System.out.println("Читатель добавлен!");
        askReturn();
    }

    private List<Book> searchBook() {
        System.out.println("*** Поиск книги ***");
        System.out.print("Введите название, автора или год: ");
        String query = scanner.nextLine().toLowerCase();

        var resBooks = library.getBooks().values().stream()
                .filter(b -> b.getTitle().toLowerCase().contains(query)
                        || b.getAuthor().toLowerCase().contains(query)
                        || String.valueOf(b.getYear()).contains(query))
                .collect(Collectors.toList());

        if (resBooks.isEmpty()) {
            System.out.println("Книг не найдено.");
            return List.of();
        } else {
            resBooks.forEach(System.out::println);
            return resBooks;
        }
    }

    private List<User> searchUser() {
        System.out.println("*** Поиск читателя ***");
        System.out.print("Введите id, имя или email: ");
        String query = scanner.nextLine().toLowerCase();

        var results = library.getUsers().values().stream()
                .filter(u -> String.valueOf(u.getId()).equals(query)
                        || u.getName().toLowerCase().contains(query)
                        || u.getEmail().toLowerCase().contains(query))
                .collect(Collectors.toList());

        if (results.isEmpty()) {
            System.out.println("Читателей не найдено.");
            return List.of();
        } else {
            results.forEach(System.out::println);
            return results;
        }
    }

    private void searchBookAndPrint() {
        searchBook();
        askReturn();
    }

    private void searchUserAndPrint() {
        searchUser();
        askReturn();
    }

    private void borrowBook() {
        System.out.println("*** Выдача книги читателю ***");
        List<User> foundUsers = searchUser();
        if (foundUsers.isEmpty()) return;

        User user;
        if (foundUsers.size() > 1) {
            System.out.print("Введите id читателя: ");
            int id = Integer.parseInt(scanner.nextLine());
            user = library.getUser(id);
            if (user == null) {
                System.out.println("Неверный id.");
                return;
            }
        } else {
            user = foundUsers.get(0);
        }

        List<Book> foundBooks = searchBook();
        if (foundBooks.isEmpty()) return;

        Book book;
        if (foundBooks.size() > 1) {
            System.out.print("Введите id книги: ");
            int id = Integer.parseInt(scanner.nextLine());
            book = library.getBook(id);
            if (book == null) {
                System.out.println("Неверный id.");
                return;
            }
        } else {
            book = foundBooks.get(0);
        }

        library.borrowBook(user.getId(), book.getId());
        System.out.println("Книга выдана.");
        askReturn();
    }

    private void returnBook() {
        System.out.println("*** Возврат книги ***");
        List<User> foundUsers = searchUser();
        if (foundUsers.isEmpty()) return;

        User user;
        if (foundUsers.size() > 1) {
            System.out.print("Введите id читателя: ");
            int id = Integer.parseInt(scanner.nextLine());
            user = library.getUser(id);
            if (user == null) {
                System.out.println("Неверный id.");
                askReturn();
                return;
            }
        } else {
            user = foundUsers.get(0);
        }

        Map<Book, Integer> userBooks = library.getUserBooks(user.getId());
        if (userBooks.isEmpty()) {
            System.out.println("У этого читателя нет книг.");
            askReturn();
            return;
        }

        System.out.println("\nКниги на руках у читателя:");
        for (Map.Entry<Book, Integer> entry : userBooks.entrySet()) {
            Book book = entry.getKey();
            Integer count = entry.getValue();
            System.out.println(book + " — " + count + " шт.");
        }

        Book bookToReturn;
        if (userBooks.size() > 1) {
            System.out.print("Введите ID книги для возврата: ");
            int bookId = Integer.parseInt(scanner.nextLine());
            bookToReturn = library.getBook(bookId);
            if (bookToReturn == null || !userBooks.containsKey(bookToReturn)) {
                System.out.println("Неверный ID книги.");
                askReturn();
                return;
            }
        } else {
            bookToReturn = userBooks.keySet().iterator().next();
        }

        library.returnBook(user.getId(), bookToReturn.getId());
        System.out.println("Книга возвращена.");
        askReturn();
    }

    private void showUserBooks() {
        System.out.println("*** Просмотр книг выданных читателю ***");
        List<User> foundUsers = searchUser();
        if (foundUsers.isEmpty()) return;

        User user;
        if (foundUsers.size() > 1) {
            System.out.print("Введите id читателя: ");
            int id = Integer.parseInt(scanner.nextLine());
            user = library.getUser(id);
            if (user == null) {
                System.out.println("Неверный id.");
                return;
            }
        } else {
            user = foundUsers.get(0);
        }

        var books = library.getUserBooks(user.getId());
        if (books.isEmpty()) {
            System.out.println("У читателя нет книг на руках.");
        } else {
            books.forEach((book, count) ->
                    System.out.println(book + " — " + count + " шт."));
        }
        askReturn();
    }

    private void showBorrowsBooks() {
        if (library.userBooks.isEmpty()) {
            System.out.println("Книг на руках у читателей сейчас нет.");
        } else {
            System.out.println("\n*** Все книги на руках ***");

            for (Map.Entry<Integer, Map<Integer, Integer>> userEntry : library.userBooks.entrySet()) {
                Integer userId = userEntry.getKey();
                Map<Integer, Integer> books = userEntry.getValue();

                User user = library.getUser(userId);
                System.out.println("Читатель: " + user);
                System.out.println("  " + "Книги на руках:");

                for (Map.Entry<Integer, Integer> bookEntry : books.entrySet()) {
                    Integer bookId = bookEntry.getKey();
                    Integer count = bookEntry.getValue();

                    Book book = library.getBook(bookId);
                    if (book != null) {
                        System.out.println("  " + book + " — " + count + " шт.");
                    }
                }
            }
        }
        askReturn();
    }

    private void showAllUsers() {
        if (library.getUsers().isEmpty()) {
            System.out.println("Нет зарегистрированных читателей.");
        } else {
            System.out.println("\n*** Все зарегистрированные читатели ***");
            library.getUsers().values().forEach(System.out::println);
        }
        askReturn();
    }

    private void showAllBooks() {
        if (library.getBooks().isEmpty()) {
            System.out.println("Нет зарегистрированных книг.");
        } else {
            System.out.println("\n*** Все зарегистрированные книги ***");
            library.getBooks().values().forEach(System.out::println);
        }
        askReturn();
    }

    private void askReturn() {
        System.out.print("Нажмите Enter для возврата в меню...");
        scanner.nextLine();
    }
}
