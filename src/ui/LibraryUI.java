package ui;

import model.Book;
import model.Library;
import model.Loan;
import model.User;

import java.util.List;
import java.util.Scanner;
import java.util.Set;
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
                    "11. Просроченные выдачи\n" +
                    "12. История выдач по пользователю\n" +
                    "13. История выдач по книге\n" +
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
                    case "11" -> showExpiredLoans();
                    case "12" -> showUserLoanHistory();
                    case "13" -> showBookLoanHistory();
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
        User newUser = new User(name, email, library);
        library.addUser(newUser);
        System.out.println(newUser);
        System.out.println("Читатель добавлен!");
        askReturn();
    }

    private List<Book> searchBook() {
        System.out.println("*** Поиск книги ***");
        System.out.print("Введите название, автора или год: ");
        String query = scanner.nextLine().toLowerCase();

        List<Book> resBooks = library.getBooks().values().stream()
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

        List<User> results = library.getUsers().values().stream()
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
        if (foundUsers.isEmpty()) {
            askReturn();
            return;
        }

        System.out.print("Введите id читателя: ");
        int userId = Integer.parseInt(scanner.nextLine());
        User user;
        user = library.getUser(userId);
        if (user == null) {
            System.out.println("Неверный id.");
            askReturn();
            return;
        }

        List<Book> foundBooks = searchBook();
        if (foundBooks.isEmpty()) {
            askReturn();
            return;
        }
        System.out.print("Введите id книги: ");
        int bookId = Integer.parseInt(scanner.nextLine());
        Book book;
        book = library.getBook(bookId);
        if (book == null) {
            System.out.println("Неверный id.");
            askReturn();
            return;
        }

        library.borrowBook(user.getId(), book.getId());
        askReturn();
    }

    private void returnBook() {
        System.out.println("*** Возврат книги ***");
        List<User> foundUsers = searchUser();
        if (foundUsers.isEmpty()) {
            askReturn();
            return;
        }
        System.out.print("Введите id читателя: ");
        int id = Integer.parseInt(scanner.nextLine());
        User user;
        user = library.getUser(id);
        if (user == null) {
            System.out.println("Неверный id.");
            askReturn();
            return;
        }

        List<Loan> userLoans = user.getCurrentLoans();
        if (userLoans.isEmpty()) {
            System.out.println("У читателя нет книг на руках.");
            askReturn();
            return;
        }

        System.out.println("\nКниги на руках у читателя:");
        userLoans.forEach(loan -> System.out.println(loan + "\n---------------------------------------------"));
        if (!userLoans.isEmpty()) {
            System.out.print("Введите ID книги для возврата: ");
            int bookId = Integer.parseInt(scanner.nextLine());
            Loan loan = userLoans.stream()
                    .filter(l -> l.getBookId() == bookId)
                    .findFirst()
                    .orElse(null);
            if (loan == null) {
                System.out.println("Неверный ID книги.");
                askReturn();
                return;
            }
            library.returnBook(user.getId(), bookId);
            System.out.println("Книга возвращена.");
            System.out.println(library.getLoan(loan.getId()));
        }
        askReturn();
    }

    private void showUserBooks() {
        System.out.println("*** Просмотр книг выданных читателю ***");
        List<User> foundUsers = searchUser();
        if (foundUsers.isEmpty()) {
            askReturn();
            return;
        }
        System.out.print("Введите id читателя: ");
        int id = Integer.parseInt(scanner.nextLine());
        User user;
        user = library.getUser(id);
        if (user == null) {
            System.out.println("Неверный id.");
            askReturn();
            return;
        } else {
            List<Loan> userLoans = user.getCurrentLoans();

            if (userLoans.isEmpty()) {
                System.out.println("У читателя нет книг на руках.");
                askReturn();
                return;
            } else {
                System.out.println("*** Все выдачи читателя ***");
                userLoans.forEach(loan -> System.out.println(loan + "\n---------------------------------------------"));
            }
        }

        askReturn();
    }

    private void showBorrowsBooks() {
        Set<Integer> activeUsers = library.getLoans().values().stream()
                .filter(loan -> !loan.isExpired())
                .map(Loan::getUserId)
                .collect(Collectors.toSet());

        if (activeUsers.isEmpty()) {
            System.out.println("Книг на руках у читателей сейчас нет.");
        } else {
            System.out.println("\n*** Все книги на руках ***");

            for (int userId : activeUsers) {
                List<Loan> userLoans = library.getUser(userId).getCurrentLoans();
                userLoans.forEach(loan -> System.out.println(loan + "\n---------------------------------------------"));
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

    private void showExpiredLoans() {
        List<Loan> expiredLoans = library.getExpiredLoans();

        if (expiredLoans.isEmpty()) {
            System.out.println("Просроченных книг не найдено.");
            askReturn();
            return;
        }

        System.out.println("\n*** Просроченные выдачи книг ***");
        expiredLoans.forEach(loan -> {
            System.out.println(loan);
            System.out.println("---------------------------------------------");
        });
        askReturn();
    }

    private void showUserLoanHistory() {
        System.out.println("*** Просмотр истории читателя ***");
        List<User> foundUsers = searchUser();
        if (foundUsers.isEmpty()) {
            askReturn();
            return;
        }

        System.out.print("Введите id читателя: ");
        int userId = Integer.parseInt(scanner.nextLine());
        User user;
        user = library.getUser(userId);
        if (user == null) {
            System.out.println("Неверный id.");
            askReturn();
            return;
        }

        List<Loan> loans = library.getUserLoanHistory(userId);
        if (loans.isEmpty()) {
            System.out.println("У пользователя нет истории получения книг.");
            askReturn();
            return;
        }

        System.out.println("\n*** История выдач пользователя ***");
        loans.forEach(loan -> {
            System.out.println(loan);
            System.out.println("---------------------------------------------");
        });
        askReturn();
    }

    private void showBookLoanHistory() {
        List<Book> foundBooks = searchBook();
        if (foundBooks.isEmpty()) {
            return;
        }

        System.out.print("Введите id книги: ");
        int bookId = Integer.parseInt(scanner.nextLine());
        Book book;
        book = library.getBook(bookId);
        if (book == null) {
            System.out.println("Неверный id.");
            askReturn();
            return;
        }

        List<Loan> loans = library.getBookLoanHistory(bookId);
        if (loans.isEmpty()) {
            System.out.println("Книга никогда не выдавалась.");
            askReturn();
            return;
        }

        System.out.println("\n*** История выдач книги ***");
        loans.forEach(loan -> {
            System.out.println(loan);
            System.out.println("---------------------------------------------");
        });
        askReturn();
    }

    private void askReturn() {
        System.out.print("Нажмите Enter для возврата в меню...");
        scanner.nextLine();
    }
}
