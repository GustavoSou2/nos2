package application.nos2.api;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ApiError handleNotFound(NoSuchElementException exception) {
        return new ApiError(OffsetDateTime.now(), HttpStatus.NOT_FOUND.value(), exception.getMessage());
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class,
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            ErrorResponseException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiError handleBadRequest(Exception exception) {
        return new ApiError(OffsetDateTime.now(), HttpStatus.BAD_REQUEST.value(), exception.getMessage());
    }

    record ApiError(
            OffsetDateTime timestamp,
            int status,
            String message
    ) {
    }
}
