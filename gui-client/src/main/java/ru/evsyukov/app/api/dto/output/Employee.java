package ru.evsyukov.app.api.dto.output;

public class Employee {

    private String employeeName;

    // признак того, есть ли отчеты у данного сотрудника
    private boolean actual;

    public Employee(String employeeName, boolean actual) {
        this.employeeName = employeeName;
        this.actual = actual;
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
}
