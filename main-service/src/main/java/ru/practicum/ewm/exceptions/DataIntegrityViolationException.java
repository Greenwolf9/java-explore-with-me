package ru.practicum.ewm.exceptions;

public class DataIntegrityViolationException extends Exception {
    public DataIntegrityViolationException(String message) {
        super(message);
    }
}
