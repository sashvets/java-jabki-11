package exceptions;

import model.User;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(User user) {
        super(String.format(
                "Читатель \"%s\" (e-mail: %s) уже зарегистрирован.",
                user.getName(),
                user.getEmail()
        ));
    }
}