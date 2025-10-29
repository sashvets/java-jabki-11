package model;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class User {
    private final int id;
    private final String name;
    private final String email;
    private Library library;

    private static final AtomicInteger counter = new AtomicInteger(0);

    public User(int id, String name, String email) {
        this.id = id;
        counter.accumulateAndGet(this.id, Math::max);
        this.name = name;
        this.email = email;
    }

    public User(String name, String email, Library library) {
        this(nextId(), name, email);
        this.library = library;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getEmail() {
        return this.email;
    }

    static int nextId() {
        return counter.incrementAndGet();
    }

    public void setLibrary(Library library) {
        this.library = library;
    }

    public List<Loan> getUserLoans() {
        return this.library.getLoans().values().stream()
                .filter(loan -> loan.getUserId() == this.id)
                .toList();
    }

    public List<Loan> getCurrentLoans() {
        return this.getUserLoans().stream()
                .filter(Loan::isActive)
                .sorted((a, b) -> b.getLoanDate().compareTo(a.getLoanDate()))
                .toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return name.equalsIgnoreCase(user.name) &&
                email.equalsIgnoreCase(user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase(), email.toLowerCase());
    }

    @Override
    public String toString() {
        return String.format("ID: %d, Имя: %s, Эл.почта: %s",
                this.id,
                this.name,
                this.email);
    }

    public String toFileString() {
        return String.format("%d;%s;%s",
                this.id,
                this.name,
                this.email);
    }

    public static User fromFileString(String line) {
        String[] parts = line.split(";");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Некорректная запись читателя: " + line);
        }

        int id = Integer.parseInt(parts[0]);
        String name = parts[1];
        String email = parts[2];

        return new User(id, name, email);
    }
}