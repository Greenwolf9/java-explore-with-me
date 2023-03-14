package ru.practicum.ewm.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.ewm.exceptions.DataIntegrityViolationException;
import ru.practicum.ewm.exceptions.IncorrectConditionException;
import ru.practicum.ewm.exceptions.NotFoundException;

import javax.validation.ValidationException;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    private ResponseEntity<ApiError> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(NotFoundException.class)
    protected ResponseEntity<ApiError> handleEntityNotFound(NotFoundException ex) {
        log.error(ex.getMessage(), ex);
        ApiError apiError = new ApiError(NOT_FOUND);
        apiError.setMessage(ex.getMessage());
        apiError.setReason("The required object was not found.");
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleIntegrityViolationException(DataIntegrityViolationException exception) {
        log.error(exception.getMessage(), exception);
        ApiError apiError = new ApiError(CONFLICT);
        apiError.setMessage(exception.getMessage());
        apiError.setReason("Integrity constraint has been violated.");
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleValidationException(ValidationException exception) {
        log.error(exception.getMessage(), exception);
        ApiError apiError = new ApiError(BAD_REQUEST);
        apiError.setMessage(exception.getMessage());
        apiError.setReason("Incorrectly made request.");
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(IncorrectConditionException.class)
    public ResponseEntity<ApiError> handleIncorrectConditionException(IncorrectConditionException exception) {
        log.error(exception.getMessage(), exception);
        ApiError apiError = new ApiError(CONFLICT);
        apiError.setMessage(exception.getMessage());
        apiError.setReason("For the requested operation the conditions are not met.");
        return buildResponseEntity(apiError);
    }
}
