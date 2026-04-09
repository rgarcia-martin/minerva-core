package com.fractalmindstudio.minerva_core.shared.interfaces.rest;

import com.fractalmindstudio.minerva_core.shared.application.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class ApiExceptionHandler {

    public static final String DEFAULT_VALIDATION_MESSAGE = "Request validation failed";
    public static final String DEFAULT_INTERNAL_ERROR_MESSAGE = "Unexpected application error";

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            final NotFoundException exception,
            final HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
            final RuntimeException exception,
            final HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            final MethodArgumentNotValidException exception,
            final HttpServletRequest request
    ) {
        final FieldError fieldError = exception.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        final String message = fieldError == null
                ? DEFAULT_VALIDATION_MESSAGE
                : fieldError.getField() + ": " + fieldError.getDefaultMessage();

        return buildResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            final Exception exception,
            final HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, DEFAULT_INTERNAL_ERROR_MESSAGE, request.getRequestURI());
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            final HttpStatus status,
            final String message,
            final String path
    ) {
        final ApiErrorResponse body = new ApiErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );

        return ResponseEntity.status(status).body(body);
    }
}
