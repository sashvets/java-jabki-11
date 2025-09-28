package model;

import java.util.Objects;

public class User {
    private final int id;
    private final String name;
    private final String email;
    private static int counter = 0;

    public User(int id, String name, String email) {
        this.id = id;
        if (counter < id) {
            counter = id;
        }
        this.name = name;
        this.email = email;
    }

    public User(String name, String email) {
        this(nextId(), name, email);
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
        counter++;
        return counter;
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
            throw new IllegalArgumentException("Неверная строка: " + line);
        }

        int id = Integer.parseInt(parts[0]);
        String name = parts[1];
        String email = parts[2];

        return new User(id, name, email);
    }
}