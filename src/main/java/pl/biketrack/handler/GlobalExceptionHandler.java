package pl.biketrack.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountStatusException.class)
    public ResponseEntity<ExceptionResponse> handleException(AccountStatusException exp) {
        return ResponseEntity.status(UNAUTHORIZED)
                             .body(ExceptionResponse.builder()
                                                    .error(exp.getMessage())
                                                    .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleException(MethodArgumentNotValidException exp) {
        Set<String> errors = new HashSet<>();
        exp.getBindingResult()
           .getAllErrors()
           .forEach(error -> {
               var errorMessage = error.getDefaultMessage();
               errors.add(errorMessage);
           });
        return ResponseEntity.status(BAD_REQUEST)
                             .body(ExceptionResponse.builder()
                                                    .validationErrors(errors)
                                                    .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception exp) {
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                             .body(ExceptionResponse.builder()
                                                    .error(exp.getMessage())
                                                    .build());
    }
}