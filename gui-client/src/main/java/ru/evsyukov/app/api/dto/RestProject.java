package ru.evsyukov.app.api.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class RestProject {

    @NotNull
    @NotEmpty
    private String projectName;

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
