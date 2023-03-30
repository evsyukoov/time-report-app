package ru.evsyukov.app.api.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class RestEmployee {

    @NotNull
    @NotEmpty
    private String name;

    @NotNull
    @NotEmpty
    private String position;

    @NotNull
    @NotEmpty
    private String department;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
