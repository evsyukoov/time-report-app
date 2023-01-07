package ru.evsyukov.polling.exceptions;

public class DateBeforeException extends RuntimeException {

    public DateBeforeException(String message) {
        super(message);
    }
}
