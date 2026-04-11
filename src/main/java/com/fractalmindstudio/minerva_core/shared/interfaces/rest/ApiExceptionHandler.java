package com.fractalmindstudio.minerva_core.shared.interfaces.rest;

import com.fractalmindstudio.minerva_core.shared.application.NotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;

@RestControllerAdvice
@Log4j2
public class ApiExceptionHandler {

    public static final String DEFAULT_VALIDATION_MESSAGE = "Request validation failed";
    public static final String DEFAULT_INTERNAL_ERROR_MESSAGE = "Unexpected application error";
    public static final String MALFORMED_REQUEST_MESSAGE = "Malformed request body";

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            final NotFoundException exception,
            final HttpServletRequest request
    ) {
        log.warn("Resource not found on {}: {}", request.getRequestURI(), exception.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEntityNotFound(
            final EntityNotFoundException exception,
            final HttpServletRequest request
    ) {
        log.warn("Persistence reference not found on {}: {}", request.getRequestURI(), exception.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class})
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
            final RuntimeException exception,
            final HttpServletRequest request
    ) {
        log.warn("Bad request on {}: {}", request.getRequestURI(), exception.getMessage());
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

        log.warn("Validation error on {}: {}", request.getRequestURI(), message);
        return buildResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotReadable(
            final HttpMessageNotReadableException exception,
            final HttpServletRequest request
    ) {
        log.warn("Malformed request body on {}", request.getRequestURI(), exception);
        return buildResponse(HttpStatus.BAD_REQUEST, MALFORMED_REQUEST_MESSAGE, request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            final MethodArgumentTypeMismatchException exception,
            final HttpServletRequest request
    ) {
        final String message = "Invalid value '%s' for parameter '%s'".formatted(exception.getValue(), exception.getName());
        log.warn("Type mismatch on {}: {}", request.getRequestURI(), message);
        return buildResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnsupportedMediaType(
            final HttpMediaTypeNotSupportedException exception,
            final HttpServletRequest request
    ) {
        log.warn("Unsupported media type on {}: {}", request.getRequestURI(), exception.getMessage());
        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotAllowed(
            final HttpRequestMethodNotSupportedException exception,
            final HttpServletRequest request
    ) {
        log.warn("Method not allowed on {}: {}", request.getRequestURI(), exception.getMessage());
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
            final DataIntegrityViolationException exception,
            final HttpServletRequest request
    ) {
        log.warn("Data integrity violation on {}", request.getRequestURI(), exception);
        return buildResponse(HttpStatus.CONFLICT, "Data integrity violation", request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            final Exception exception,
            final HttpServletRequest request
    ) {
        log.error("Unexpected application error on {}", request.getRequestURI(), exception);
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
