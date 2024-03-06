package ru.evsyukov.app.api.dto.output;

public class Employee {

    private String employeeName;

    // признак того, есть ли отчеты у данного сотрудника
    private boolean actual;

    // сотрудник перенесен в архив через админ-панель
    private boolean archived;

    public Employee(String employeeName, boolean actual, boolean archived) {
        this.employeeName = employeeName;
        this.actual = actual;
        this.archived = archived;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public boolean isActual() {
        return actual;
    }

    public void setActual(boolean actual) {
        this.actual = actual;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }
}
