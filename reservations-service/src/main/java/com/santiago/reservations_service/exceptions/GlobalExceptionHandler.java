package com.santiago.reservations_service.exceptions;

import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import feign.FeignException;

// Respuestas de error con un "message" claro, sin exponer detalles internos.
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> notFound(NotFoundException e) {
        return body(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, String>> conflict(ConflictException e) {
        return body(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> badRequest(BadRequestException e) {
        return body(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, String>> remoteFailure(FeignException e) {
        log.error("Fallo llamando a otro microservicio: {}", e.getMessage());
        return body(HttpStatus.SERVICE_UNAVAILABLE,
                "No fue posible completar la operacion en este momento, intenta de nuevo mas tarde");
    }

    private ResponseEntity<Map<String, String>> body(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Collections.singletonMap("message", message));
    }
}
