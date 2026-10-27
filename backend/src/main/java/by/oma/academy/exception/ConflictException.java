package by.oma.academy.exception;

/** 409 Conflict — например, дублирующийся email. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
