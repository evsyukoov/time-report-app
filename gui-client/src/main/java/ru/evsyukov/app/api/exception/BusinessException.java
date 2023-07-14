package ru.evsyukov.app.api.exception;

import ru.evsyukov.app.api.dto.Status;

public class BusinessException extends RuntimeException {

    private Status reason;

    public BusinessException(Status reason) {
        this.reason = reason;
    }

    public Status getReason() {
        return reason;
    }
}
