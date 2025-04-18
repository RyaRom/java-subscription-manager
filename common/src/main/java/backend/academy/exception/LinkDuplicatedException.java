package backend.academy.exception;

public class LinkDuplicatedException extends RuntimeException {
    public LinkDuplicatedException(String message) {
        super(message);
    }
}
