package ru.evsyukov.app.api.dto.output;

public class Project {

    private String projectName;

    // признак для понимания используется ли сейчас данный проект где-то в отчетах
    private boolean used;

    private boolean archived;

    public Project(String projectName, boolean used, boolean archived) {
        this.projectName = projectName;
        this.used = used;
        this.archived = archived;
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

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }
}
