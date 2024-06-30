package ru.evsyukov.app.data.entity;

import javax.persistence.*;

@Entity
@Table(name = "employee_dictionary")
public class Employee implements Cloneable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name="name")
    private String name;

    @Column(name = "position")
    private String position;

    @Column(name = "department")
    private String department;

    @Column(name="department_short")
    private String departmentShort;

    @Column(name = "archived")
    private boolean archived;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getDepartmentShort() {
        return departmentShort;
    }

    public void setDepartmentShort(String departmentShort) {
        this.departmentShort = departmentShort;
    }


    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        Employee employee = new Employee();
        employee.setDepartmentShort(this.departmentShort);
        employee.setDepartment(this.department);
        employee.setName(this.name);
        employee.setPosition(this.position);
        employee.setId(this.id);
        return employee;
    }
}
