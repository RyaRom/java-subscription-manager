package backend.academy.exception;

public class TelegramServerError extends RuntimeException {
    public TelegramServerError(Throwable cause) {
        super(cause);
    }
}
