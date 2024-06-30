package ru.evsyukov.app.api.dto.output;

import lombok.Builder;
import lombok.Data;
import ru.evsyukov.app.api.dto.output.Status;

@Builder
@Data
public class OperationResponse {

    private Status status;

    private String description;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
