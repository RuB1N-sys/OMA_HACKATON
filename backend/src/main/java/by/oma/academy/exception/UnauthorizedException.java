package by.oma.academy.exception;

/** 401 Unauthorized — неверные учётные данные или недействительный токен. */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
