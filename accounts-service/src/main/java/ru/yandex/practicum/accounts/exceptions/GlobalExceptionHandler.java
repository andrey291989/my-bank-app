package ru.yandex.practicum.accounts.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleAccountNotFound(AccountNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(
                HttpStatus.NOT_FOUND,
                "Account not found",
                ex.getMessage()
        ));
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientFunds(InsufficientFundsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Insufficient funds",
                ex.getMessage()
        ));
    }

    @ExceptionHandler(UnderAgeException.class)
    public ResponseEntity<Map<String, Object>> handleUnderAge(UnderAgeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Under age",
                ex.getMessage()
        ));
    }

    @ExceptionHandler(InvalidTransferAmountException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidTransferAmount(InvalidTransferAmountException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(createErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid transfer amount",
                ex.getMessage()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                "An unexpected error occurred"
        ));
    }

    private Map<String, Object> createErrorResponse(HttpStatus status, String error, String message) {
        return Map.of(
                "timestamp", LocalDateTime.now(),
                "status", status.value(),
                "error", error,
                "message", message
        );
    }
}