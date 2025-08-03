package es.demo.esdemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import es.demo.esdemo.repository.VersionMismatch;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.UUID;

@ControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createApiError(ex));
    }

    @ExceptionHandler(VersionMismatch.class)
    public ResponseEntity<ApiError> handleVersionMismatch(VersionMismatch ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createApiError(ex));

    }

    private static ApiError createApiError(Exception ex) {
        var errorId = UUID.randomUUID().toString().substring(0, 8);
        log.error("API Error [{}]: {}", errorId, ex.getMessage(), ex);
        return new ApiError(errorId, ex.getMessage());
    }

    public record ApiError(String errorId, String message) {

    }

}