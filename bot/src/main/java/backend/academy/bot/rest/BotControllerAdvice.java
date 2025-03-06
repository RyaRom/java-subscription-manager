package backend.academy.bot.rest;

import backend.academy.dto.ApiErrorResponse;
import java.util.stream.Stream;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.MissingRequestValueException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import reactor.core.publisher.Mono;

@Log4j2
@RestControllerAdvice
public class BotControllerAdvice {

    @ExceptionHandler({
        IllegalArgumentException.class,
        MissingRequestValueException.class,
        UnsupportedMediaTypeStatusException.class
    })
    public Mono<ResponseEntity<ApiErrorResponse>> handleBadRequestExceptions(Exception e) {
        log.error("Bad request error: {}", e.getMessage());
        return Mono.just(ResponseEntity.badRequest().body(ApiErrorResponse.builder()
            .code("400")
            .exceptionName(e.getClass().getName())
            .exceptionMessage(e.getMessage())
            .build()));
    }


    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiErrorResponse>> unknownException(Exception e) {
        log.error(e.getMessage());
        return Mono.just(ResponseEntity.internalServerError().body(ApiErrorResponse.builder()
            .code("500")
            .exceptionName(e.getClass().getName())
            .stackTrace(Stream.of(e.getStackTrace()).map(StackTraceElement::toString).toList())
            .exceptionMessage(e.getMessage())
            .build()));
    }
}
