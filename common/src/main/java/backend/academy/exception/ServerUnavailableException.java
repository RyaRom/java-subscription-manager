package backend.academy.exception;

public class ServerUnavailableException extends RuntimeException {
    public ServerUnavailableException(String message) {
        super(message);
    }

    public ServerUnavailableException() {
        super();
    }
}
