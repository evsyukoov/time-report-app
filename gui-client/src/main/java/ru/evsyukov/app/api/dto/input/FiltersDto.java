package ru.evsyukov.app.api.dto.input;

import java.util.Date;

public class FiltersDto {

    private String name;

    private Date dateStart;

    private Date dateEnd;

    private String department;

    private String project;

    private boolean waitForEmployeeReport;

    private boolean waitForDepartmentsReport;

    private boolean waitForProjectReport;

    private boolean waitForAllProjectsReport;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDateStart() {
        return dateStart;
    }

    public void setDateStart(Date dateStart) {
        this.dateStart = dateStart;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public boolean isWaitForEmployeeReport() {
        return waitForEmployeeReport;
    }

    public void setWaitForEmployeeReport(boolean waitForEmployeeReport) {
        this.waitForEmployeeReport = waitForEmployeeReport;
    }

    public boolean isWaitForDepartmentsReport() {
        return waitForDepartmentsReport;
    }

    public boolean isWaitForProjectReport() {
        return waitForProjectReport;
    }

    public void setWaitForProjectReport(boolean waitForProjectReport) {
        this.waitForProjectReport = waitForProjectReport;
    }

    public void setWaitForDepartmentsReport(boolean waitForDepartmentsReport) {
        this.waitForDepartmentsReport = waitForDepartmentsReport;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public boolean isWaitForAllProjectsReport() {
        return waitForAllProjectsReport;
    }

    public void setWaitForAllProjectsReport(boolean waitForAllProjectsReport) {
        this.waitForAllProjectsReport = waitForAllProjectsReport;
    }
}
