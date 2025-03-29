package backend.academy.exception;

public class BadLinkException extends RuntimeException {
    public BadLinkException(String message) {
        super(message);
    }
}
