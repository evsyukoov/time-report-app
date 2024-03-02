package ru.evsyukov.app.api.dto.output;

public class Project {

    private String projectName;

    // признак для понимания используется ли сейчас данный проект где-то в отчетах
    private boolean used;

    public Project(String projectName, boolean used) {
        this.projectName = projectName;
        this.used = used;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}
