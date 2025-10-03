import model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UserTest {
    @Test
    void testUserConstructor() {
        User user = new User(1, "Никита", "nik@mail.ru");
        Assertions.assertEquals(1, user.getId());
        Assertions.assertEquals("Никита", user.getName());
        Assertions.assertEquals("nik@mail.ru", user.getEmail());
    }

    @Test
    void testEqualsAndHashCode() {
        User user1 = new User(1, "Николай", "nik@mail.ru");
        User user2 = new User(2, "николай", "NIK@mail.ru");
        User user3 = new User(1, "Никита", "nik@mail.ru");
        Assertions.assertEquals(user1, user2);
        Assertions.assertEquals(user1.hashCode(), user2.hashCode());
        Assertions.assertNotEquals(user2, user3);
    }

    @Test
    void testToString() {
        User user = new User(7, "Никита", "nik@mail.ru");
        Assertions.assertEquals("ID: 7, Имя: Никита, Эл.почта: nik@mail.ru", user.toString());
    }

    @Test
    void testToFileStringAndFromFileString() {
        User user = new User(9, "Никита", "nik@mail.ru");
        String lineUser = user.toFileString();
        Assertions.assertEquals("9;Никита;nik@mail.ru", lineUser);
        User userFromLine = User.fromFileString(lineUser);
        Assertions.assertEquals(user, userFromLine);
        Assertions.assertEquals(user.getId(), userFromLine.getId());
    }
}