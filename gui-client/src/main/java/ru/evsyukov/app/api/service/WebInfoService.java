package ru.evsyukov.app.api.service;

import ru.evsyukov.app.api.dto.output.Department;
import ru.evsyukov.app.api.dto.output.Employee;
import ru.evsyukov.app.api.dto.output.Project;

import java.util.List;

public interface WebInfoService {

    List<Employee> getEmployees();

    List<Department> getDepartments();

    List<Project> getProjects();

}
