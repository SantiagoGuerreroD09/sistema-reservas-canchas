package com.santiago.courts_service.exceptions;

// Regla de negocio violada por el estado actual (ej. horario ya tomado) -> 409.
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
