package com.santiago.reservations_service.exceptions;

// Datos de entrada invalidos -> 400.
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
