package es.demo.esdemo.repository.exceptions;

public class InvalidEventException extends RuntimeException{
    public InvalidEventException() {
    }

    public InvalidEventException(String message) {
        super("invalid event: " + message);
    }
}