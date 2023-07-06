package ru.evsyukov.app.api.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class OperationResponse {

    private Status status;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
